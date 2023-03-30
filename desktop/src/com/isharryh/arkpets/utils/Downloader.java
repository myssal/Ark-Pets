/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


public class Downloader {
    public static GitHubSource[] ghSources = new GitHubSource[] {
            new GitHubSource("GitHub", "https://raw.githubusercontent.com/", "https://github.com/"),
            new GitHubSource("FastGit", "https://raw.fastgit.org/", "https://download.fastgit.org/"),
            new GitHubSource("GHProxy", "https://ghproxy.com/?q=https://github.com/")
    };

    public void httpsDownload(String $fromPath, String $toPath, int $timeoutMillis, int $bufferSize, boolean $insecure) {
        URL urlFile;
        HttpsURLConnection connection = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        File file = new File($toPath);
        try {
            urlFile = new URL($fromPath);
            connection = IOUtils.NetUtil.createHttpsConnection(urlFile, $timeoutMillis, $timeoutMillis, $insecure);
            bis = new BufferedInputStream(connection.getInputStream(), $bufferSize);
            bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()), $bufferSize);
        } catch (IOException e) {
            try {
                if (connection != null && connection.getInputStream() != null)
                    connection.getInputStream().close();
                if (bis != null)
                    bis.close();
                if (bos != null)
                    bos.close();
            } catch (Exception ignored){
            }
            onFail(e);
        }
        int len = $bufferSize;
        long sum = 0;
        long max = connection.getContentLengthLong();
        byte[] bytes = new byte[len];
        try {
            onStart(max);
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
                sum += len;
                if (!onFetch(len, sum, max))
                    break;
            }
            bos.flush();
            onSucceed(sum);
        } catch (IOException e) {
            onFail(e);
        } finally {
            try {
                if (connection != null && connection.getInputStream() != null)
                    connection.getInputStream().close();
                if (bis != null)
                    bis.close();
                if (bos != null)
                    bos.close();
            } catch (Exception ignored){
            }
        }
    }

    protected void onStart(long max) {
    }

    protected boolean onFetch(int len, long sum, long max) {
        return true;
    }

    protected void onFail(Exception e) {
    }

    protected void onSucceed(long sum) {
    }

    /** Test the real connection delay of the given URL (with a specified port).
     * @param $url The URL to be tested.
     * @param $port The port to connect.
     * @param $timeoutMillis Timeout (ms).
     * @return The delay (ms). {@code -1} when connection failed or timeout.
     */
    public static int testDelay(String $url, int $port, int $timeoutMillis) {
        Socket socket = new Socket();
        int delayMillis = -1;
        try {
            SocketAddress address = new InetSocketAddress(new URL($url).getHost(), $port);
            long start = System.currentTimeMillis();
            socket.connect(address, $timeoutMillis);
            long stop = System.currentTimeMillis();
            delayMillis = (int)(stop - start);
        } catch (IOException ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        return delayMillis;
    }


    public static class Source {
        public final String tag;
        public final String preUrl;
        public int delay = -1;

        public Source(String $tag, String $preUrl) {
            tag= $tag;
            preUrl = $preUrl;
        }

        public int testDelay() {
            return testDelay(443, 2000);
        }

        public int testDelay(int $port, int $timeoutMillis) {
            delay = Downloader.testDelay(preUrl, $port, $timeoutMillis);
            Logger.debug("Downloader", "Real delay for \"" + tag + "\" is " + delay + "ms");
            return delay;
        }

        public static Source[] sortByDelay(Source[] $sources) {
            for (Source s : $sources)
                s.testDelay();
            ArrayList<Source> sources = new ArrayList<>(Arrays.stream($sources).toList());
            sources.sort((o1, o2) -> {
                if (o1.delay == o2.delay)
                    return 0;
                return (o1.delay > o2.delay || o1.delay < 0) ? 1 : -1;
            });
            return sources.toArray(new Source[0]);
        }
    }


    public static class GitHubSource extends Source {
        public final String rawPreUrl;
        public final String archivePreUrl;

        public GitHubSource(String $tag, String $preUrl) {
            super($tag, $preUrl);
            rawPreUrl = $preUrl;
            archivePreUrl = $preUrl;
        }

        public GitHubSource(String $tag, String $rawPreUrl, String $archivePreUrl) {
            super($tag, $rawPreUrl);
            rawPreUrl = $rawPreUrl;
            archivePreUrl = $archivePreUrl;
        }

        public static GitHubSource[] sortByDelay(GitHubSource[] $sources) {
            for (GitHubSource s : $sources)
                s.testDelay();
            ArrayList<GitHubSource> sources = new ArrayList<>(Arrays.stream($sources).toList());
            sources.sort((o1, o2) -> {
                if (o1.delay < 0 && o2.delay >= 0)
                    return 1;
                if (o1.delay != o2.delay)
                    return o1.delay > o2.delay ? 1 : -1;
                return 0;
            });
            return sources.toArray(new GitHubSource[0]);
        }
    }
}
