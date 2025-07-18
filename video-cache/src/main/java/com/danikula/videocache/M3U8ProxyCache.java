package com.danikula.videocache;

import static com.danikula.videocache.ProxyCacheUtils.DEFAULT_BUFFER_SIZE;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.coolerfall.download.DownloadCallback;
import com.coolerfall.download.DownloadManager;
import com.coolerfall.download.DownloadRequest;
import com.coolerfall.download.OkHttpDownloader;
import com.danikula.videocache.file.FileCache;
import com.danikula.videocache.parser.Element;
import com.danikula.videocache.parser.Playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class M3U8ProxyCache extends HttpProxyCache {
    private static final Logger LOG = LoggerFactory.getLogger("M3U8ProxyCache");

    //原始文件
    private static final String LOCAL_M3U8_SUFFIX = "_local.m3u8";
    //修改后
    private static final String PROXY_M3U8_SUFFIX = "_proxy.m3u8";

    /**
     * 是否开启广告过滤
     */
    public static boolean adFilter = false;

    private static final int M3U8_CACHE_STEP = 20;
    private File m3u8CacheDir;
    private final DownloadManager m3u8Downloader;
    private String baseUrl;
    private String baseCachePath;
    private List<Element> elements;
    private final Map<String, M3U8Item> m3U8ItemMap = new HashMap<>();
    private int mCurRequestPos = 0; // 客户端正在请求的ts索引
    private int mCurCachePos = 0;   // 代理端正在缓存的，离mCurRequestPose最远的ts索引
    private int mCacheShift = 2;    // 缓存位置与播放位置偏移，对需要ts需要解码的情况，就不能有偏移了，必须全部走缓存流程
    private final DecryptInfo mDecryptInfo;
    private volatile boolean mTransformed = false;


    static class M3U8Item {
        public Element element;
        public DownloadRequest dlRequest;

        public M3U8Item(Element element, DownloadRequest request) {
            this.element = element;
            this.dlRequest = request;
        }

        public M3U8Item(Element element) {
            this(element, null);
        }
    }

    public static class DecryptInfo {
        public int step;
        public String key;

        public DecryptInfo(String key, int step) {
            this.key = key;
            this.step = step;
        }
    }

    public static class CacheItem {
        public DecryptInfo decryptInfo;
        public String filePath;

        public CacheItem(String filePath, DecryptInfo decryptInfo) {
            this.filePath = filePath;
            this.decryptInfo = decryptInfo;
        }
    }

    public M3U8ProxyCache(Context context, HttpUrlSource source, FileCache cache) {
        super(source, cache);
        m3u8Downloader = new DownloadManager.Builder()
                .context(context).downloader(OkHttpDownloader.create()).build();

        baseUrl = source.getUrl();
        int end = baseUrl.indexOf("?");
        if (end > 0)
            baseUrl = baseUrl.substring(0, end);

        end = baseUrl.lastIndexOf("/");
        baseUrl = baseUrl.substring(0, end + 1);

        Log.d("jdy", "baseUrl: " + baseUrl);

        mDecryptInfo = new DecryptInfo(source.getKey(), 512);
    }

    private final DownloadCallback callback = new DownloadCallback() {
        @Override
        public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
            int percents = 100 * mCurCachePos / elements.size();
            int subPercents = (int) (100 * bytesWritten / totalBytes / elements.size());
            if (listener != null) {
                listener.onCacheAvailable(cache.file, source.getUrl(), percents + subPercents);
            }
        }

        @Override
        public void onSuccess(int downloadId, String filePath) {
            if (listener != null) {
                listener.onM3U8ItemDecrypt(new CacheItem(filePath, mDecryptInfo));
            }

            scheduleCache();
        }
    };

    private String getTsRelativeName(int index, String url) {
        int p = url.lastIndexOf('/');
        if (p > 0)
            return index + "_" + url.substring(p + 1);
        else
            return index + "_" + url;
    }

    private String getRelativeKey(String line) {
        int start = line.indexOf("http");
        int end = line.lastIndexOf('/');
        return line.substring(0, start) + line.substring(end + 1);
    }

    private void scheduleCache() {
        // 从当前正在请求的位置mCurRequestPose往后偏移若干个ts开始下载， 偏移量由mCacheShift控制
        int start = mCurRequestPos + mCacheShift;
        int end = start + M3U8_CACHE_STEP;
        if (end >= elements.size()) {
            end = elements.size();
        }

        for (int pos = start; pos < end; pos++) {
            Element element = elements.get(pos);
            String name = element.getURI().toString();
            String url;
            if (name.startsWith("http")) {
                url = name;
            } else if (name.startsWith("/")) {
                url = baseUrl.substring(0, baseUrl.indexOf('/', 10)) + name;
            } else {
                url = baseUrl + name;
            }
            name = getTsRelativeName(pos, name);

            File file = new File(baseCachePath + File.separator + name);
            if (!file.exists()) {
                DownloadRequest request = new DownloadRequest.Builder()
                        .destinationFilePath(baseCachePath + File.separator + name)
                        .downloadCallback(callback)
                        .retryTime(2)
                        .allowedNetworkTypes(DownloadRequest.NETWORK_WIFI | DownloadRequest.NETWORK_MOBILE)
                        .progressInterval(1000, TimeUnit.MILLISECONDS)
                        .url(url) // fixme header inject
                        .build();

                m3u8Downloader.add(request);

                mCurCachePos = pos + 1;
                break;
            }
        }
    }

    private void tryTransformList(File raw) {
        try {
            File ft = new File(raw.getAbsolutePath() + ".t");
            InputStreamReader inputreader = new InputStreamReader(new FileInputStream(raw));
            BufferedReader buffreader = new BufferedReader(inputreader);
            FileOutputStream fos = new FileOutputStream(ft);

            String line;
            //分行读取
            int index = 0;
            while ((line = buffreader.readLine()) != null) {
                if (line.endsWith(".ts") || line.endsWith(".jpg")) {
                    //ts文件
                    line = getTsRelativeName(index, line);
                    index++;
                }
                fos.write((line + "\n").getBytes());
            }

            fos.flush();
            raw.renameTo(new File(raw.getParentFile(), raw.getName() + LOCAL_M3U8_SUFFIX));
            ft.renameTo(raw);

            cache.reload();

            buffreader.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCachePercentsAvailableChanged(int percents) {
        if (cache.isCompleted()) {
            parseM3U8();
        }
    }

    private void parseM3U8() {
        try {
            if (mTransformed) {
                return;
            }
            // 此处的cache代表的是m3u8的清单文件，清单文件下载完成后，将所有ts信息解析出来
            boolean hasLocalFile = false;
            File raw = new File(cache.file.getParentFile(), cache.file.getName() + LOCAL_M3U8_SUFFIX);
            if (raw.exists() && raw.length() > 0) {
                hasLocalFile = true;
            } else {
                raw = cache.file;
            }
            FileInputStream fis = new FileInputStream(raw);
            elements = Playlist.parse(fis).getElements();

            String name;
            int index = 0;
            for (Element element : elements) {
                // 使用相对名称作为key
                name = getTsRelativeName(index, element.getURI().getPath());
                // Log.d("jdy", "name: " + name);
                m3U8ItemMap.put(name, new M3U8Item(element));
                index++;
            }

            if (!hasLocalFile) {
                // 处理清单文件，将其中ts全部改为相对路径
                tryTransformList(cache.file);
            }
            mTransformed = true;

            if (TextUtils.isEmpty(mDecryptInfo.key))
                mCacheShift = 2;
            else
                mCacheShift = 0;

            // 创建ts文件下载文件夹
            baseCachePath = cache.file.getAbsolutePath() + "s";
            m3u8CacheDir = new File(baseCachePath);
            m3u8CacheDir.mkdirs();

            // 调度下载第一批文件
            scheduleCache();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected boolean isUseCache(GetRequest request) throws ProxyCacheException {
        String uri = ProxyCacheUtils.decode(request.uri);
        if (ProxyCacheUtils.isM3U8(uri)) {
            return super.isUseCache(request);
        }

        return isTsDownloaded(request);
    }

    private boolean isTsDownloaded(GetRequest request) {
        if (m3u8CacheDir == null || !m3u8CacheDir.exists())
            return false;

        File ele = new File(m3u8CacheDir.getAbsolutePath() + File.separator + request.uri);
        return ele.exists();
    }

    private long getTsFileLength(GetRequest request) {
        File ele = new File(m3u8CacheDir.getAbsolutePath() + File.separator + request.uri);
        if (ele.exists()) {
            return ele.length();
        } else {
            return -1;
        }
    }

    private void responseWithoutCache(OutputStream out, GetRequest request, HttpUrlSource newSourceNoCache) throws ProxyCacheException, IOException {
        String childPath;
        if (request.uri.endsWith(".key")) {
            childPath = request.uri.hashCode() + ".key";
        } else {
            childPath = request.uri;
        }
        try {
            // 在返回从服务器直取文件数据时，同时将数据保存在本地
            File local = new File(m3u8CacheDir.getAbsolutePath() + File.separator + childPath + ".caching");
            if (local.exists()) {
                local.delete();
            }
            local.createNewFile();

            FileOutputStream fos = new FileOutputStream(local);
            long offset = request.rangeOffset;

            newSourceNoCache.open(offset);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int readBytes;
            while ((readBytes = newSourceNoCache.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
                fos.write(buffer, 0, readBytes);

                offset += readBytes;
            }
            out.flush();
            fos.flush();
            fos.close();
            out.close();

            // 数据取完后，重命名为正式文件
            local.renameTo(new File(m3u8CacheDir.getAbsolutePath() + File.separator + childPath));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            newSourceNoCache.close();
        }
    }

    private boolean waitFileExists(File dst, int tick) {
        while (tick-- > 0) {
            if (dst.exists())
                return true;

            try {
                Log.i("vcache", "wait " + dst.getName() + " for " + tick);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    protected void responseWithEncryptedCache(OutputStream out, GetRequest request) throws IOException {
        String childPath;
        if (request.uri.endsWith(".key")) {
            childPath = request.uri.hashCode() + ".key";
        } else {
            childPath = request.uri;
        }
        String path = m3u8CacheDir.getAbsolutePath() + File.separator + childPath;
        File origin = new File(path);

        FileWriter fw = new FileWriter(origin);

        String url = baseUrl.substring(0, baseUrl.indexOf('/', 10) + 1) + request.uri;

        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(request.uri).openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setReadTimeout(10 * 1000);
        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String key = br.readLine();
        fw.write(key);
        fw.close();
        br.close();

        /*File decrypted = new File(path + ".d");
        Log.d("jdy", "decryptedPath: " + decrypted.getPath());

        waitFileExists(decrypted, 60);

        decrypted.renameTo(origin);*/

        responseWithCache(out, request);
    }

    protected void responseWithCache(OutputStream out, GetRequest request) throws IOException {
        String childPath;
        if (request.uri.endsWith(".key")) {
            childPath = request.uri.hashCode() + ".key";
        } else {
            childPath = request.uri;
        }
        FileInputStream fis = new FileInputStream(m3u8CacheDir.getAbsolutePath() + File.separator + childPath);

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readBytes;
        while ((readBytes = fis.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, readBytes);
        }
        out.flush();
        fis.close();
    }

    // 返回ts文件请求的响应头， 与父类同名文件相比， 主要ts文件长度计算方式不一样
    protected String newResponseHeaders(GetRequest request, HttpUrlSource source) throws IOException, ProxyCacheException {
        String mime = source.getMime();
        long length = getTsFileLength(request);
        if (length <= 0) {
            length = source.length();
        }
        boolean lengthKnown = length >= 0;
        long contentLength = request.partial ? length - request.rangeOffset : length;
        boolean addRange = lengthKnown && request.partial;
        return new StringBuilder()
                .append(request.partial ? "HTTP/1.1 206 PARTIAL CONTENT\n" : "HTTP/1.1 200 OK\n")
                .append("Accept-Ranges: bytes\n")
                .append(lengthKnown ? format("Content-Length: %d\n", contentLength) : "")
                .append(addRange ? format("Content-Range: bytes %d-%d/%d\n", request.rangeOffset, length - 1, length) : "")
                .append(TextUtils.isEmpty(mime) ? "" : format("Content-Type: %s\n", mime))
                .append("\n") // headers end
                .toString();
    }

    @Override
    public void processRequest(GetRequest request, Socket socket) throws IOException, ProxyCacheException {
        String uri = ProxyCacheUtils.decode(request.uri);
        if (ProxyCacheUtils.isM3U8(uri)) {
            // Log.d("jdy", "进入 ProxyCacheUtils.isM3U8");
            // 等待清单文件预处理（去除其中绝对路径）完成
            if (cache.isCompleted()) {
                // Log.d("jdy", "进入 cache.isCompleted");
                parseM3U8();
            } else {
                // Log.d("jdy", "进入 ! cache.isCompleted");

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int readBytes, offset = 0;
                while ((readBytes = read(buffer, offset, buffer.length)) != -1) {
                    offset += readBytes;
                }

                int tick = 60;
                while (tick-- > 0) {
                    if (mTransformed)
                        break;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 清单文件的请求，让父类处理
            super.processRequest(request, socket);
        } else {
            // 查询当前请求的ts文件在m3u8中的位置
            M3U8Item item = m3U8ItemMap.get(uri);
            String url;
            if (item != null) {
                mCurRequestPos = elements.indexOf(item.element);
                url = item.element.getURI().toString();
                // Log.d("jdy", "item: " + item.element);
            } else {
                // 解密路径
                url = uri;
            }

            // Log.d("jdy", "beforeUrl: " + url);

            boolean hasKey = false;

            // 计算ts文件在服务器的url， 相对路径的， 要加上base
            if (!url.startsWith("http")) {
                if (url.endsWith(".key")) {
                    hasKey = true;
                    // TODO: 2025/5/15 这里得改一下，可能其他key的地址不是这样的
                    url = baseUrl.substring(0, baseUrl.indexOf('/', 10) + 1) + url;
                } else if (url.startsWith("/")) {
                    url = baseUrl.substring(0, baseUrl.indexOf('/', 10)) + url;
                } else {
                    url = baseUrl + url;
                }
            }
            Log.d("jdy", "url: " + url);

            HttpUrlSource newSourceNoCache = new HttpUrlSource(this.source, new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(uri)));
            String responseHeaders = newResponseHeaders(request, newSourceNoCache);
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            out.write(responseHeaders.getBytes("UTF-8"));

            scheduleCache();

            if (!TextUtils.isEmpty(mDecryptInfo.key) || hasKey) {
                // 需解密，强制缓存并解密
                // responseWithEncryptedCache(out, request);
                responseWithoutCache(out, request, newSourceNoCache);
            } else if (isUseCache(request)) {
                // 本地已缓存， 从文件中读取数据返回
                responseWithCache(out, request);
            } else {
                // 本地未缓存， 从服务器读取数据返回
                responseWithoutCache(out, request, newSourceNoCache);
            }
            out.close();
        }
    }

}
