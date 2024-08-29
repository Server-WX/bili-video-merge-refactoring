package club.dongfang7su;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    private static JsonObject getReader(File file) throws FileNotFoundException {
        JsonObject jsonObject;
        JsonReader reader = Json.createReader(new FileReader(file.getPath()));
        jsonObject = reader.readObject();
        return jsonObject;
    }

    private static void outputFile(String command, String output) {
        try {
//            System.out.println(command);
            Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            // 获取输出流
            InputStream inputStream = videoProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            // 读取输出内容并打印
            String line;
            while ((line = reader.readLine()) != null) {
                // 清空命令行
                System.out.print("\033[H\033[2J");
                System.out.flush();
                // 移动光标到命令行顶部
                System.out.print("\033[0;0H");
                // 输出固定显示的文字
                System.out.println("----------------------------------------");
                System.out.println("正在导出中，请勿关闭程序！！！！！！！！");
                System.out.println("----------------------------------------");

                // 输出日志
                System.out.println(line);
            }
            int exitCode = videoProcess.waitFor();
            if (exitCode == 0) {
                System.out.println("合并成功，文件：" + output);
            } else {
                System.out.println("合并失败，错误代码：" + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String folderPath = null;

        final ArrayList<File> jsonPathList = new ArrayList<>();
        final ArrayList<String> indexList = new ArrayList<>();
        final ArrayList<String> indexTitleList = new ArrayList<>();
        final ArrayList<String> fileNameList = new ArrayList<>();
        final ArrayList<File> videoPathList = new ArrayList<>();
        final ArrayList<File> audioPathList = new ArrayList<>();
        final ArrayList<String> videoName = new ArrayList<>();

        for (String arg : args) {
            if (new File(arg).exists()) {
                folderPath = arg.replace("\\", "\\\\");     //  读取拖拽文件路径
            }
        }

//        folderPath = "D:\\桌面\\A";
//        folderPath = "D:\\桌面\\s_32436";

        if (folderPath != null) {
            for (File rootFolder : Objects.requireNonNull(new File(folderPath).listFiles())) {
                File rootExistsFolder = new File(rootFolder + "\\entry.json");
                if (!rootExistsFolder.exists()) {
//                    System.out.println(rootExistsFolder);
                    for (File childFolder : Objects.requireNonNull(rootFolder.listFiles())) {
//                        System.out.println(childFolder.getPath());
                        if (new File(childFolder + "\\entry.json").exists()) {
                            jsonPathList.add(new File(childFolder + "\\entry.json"));
                        }
                        for (File elementFolder : Objects.requireNonNull(childFolder.listFiles())) {
                            if (elementFolder.isDirectory()) {
                                videoPathList.add(new File(elementFolder + "\\video.m4s"));
                                audioPathList.add(new File(elementFolder + "\\audio.m4s"));
                            }
                        }
                    }
                } else {
//                    System.out.println(rootExistsFolder.exists());
                    jsonPathList.add(rootExistsFolder);
                    for (File elementFolder : Objects.requireNonNull(rootFolder.listFiles())) {
                        if (elementFolder.isDirectory()) {
                            videoPathList.add(new File(elementFolder + "\\video.m4s"));
                            audioPathList.add(new File(elementFolder + "\\audio.m4s"));
                        }
                    }
                }
            }
        }

        String cartoon = "ep";
        String userVideo = "page_data";

        for (File file : jsonPathList) {
//            System.out.println(file.getPath());
            try {
                JsonObject object = getReader(file);
                videoName.add(getReader(file).getString("title").replace(" ", "_"));
                if (object.getJsonObject(cartoon) != null) {
                    indexList.add(object.getJsonObject(cartoon).getString("index"));
                    indexTitleList.add(object.getJsonObject(cartoon).getString("index_title"));
                } else if (object.getJsonObject(userVideo) != null) {
                    indexList.add(object.getJsonObject(userVideo).getString("download_subtitle"));
                    indexTitleList.add(object.getJsonObject(userVideo).getString("part"));
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        for (String index : indexList) {
            if (index.matches("[-+]?\\d*\\.?\\d+")) {
                String indexVideo = "第" + index + "集";
                String videoTitle = indexTitleList.get(indexList.indexOf(index)).replace(" ", "_");
                if (indexTitleList.get(indexList.indexOf(index)).isEmpty()) {
                    fileNameList.add(indexVideo);
                } else fileNameList.add(indexVideo + "-" + videoTitle);
            } else fileNameList.add(index);
        }

        String ffmpegPath = "ffmpeg\\ffmpeg.exe"; // TODO: 注意替换ffmpeg程序路径
        String fileFolder;
        for (int i = 0; i < videoPathList.size(); i++) {
            fileFolder = new File(folderPath).getParent();
            File videoFile = videoPathList.get(i);
            File audioFile = audioPathList.get(i);
            String output = fileFolder + "\\output\\" + videoName.get(i) + "\\" + videoName.get(i) + " " + fileNameList.get(i) + ".mp4";
            File pathInfo = new File(fileFolder + "\\output\\" + videoName.get(i));
            if (!pathInfo.exists())
                System.out.println(pathInfo + " 文件夹创建：" + pathInfo.mkdirs());

//            String NVIDIA_GPU = "\"" + ffmpegPath + "\"" + " -i " + "\"" + videoFile + "\"" + " -i " + "\"" + audioFile + "\"" + " -codec copy -c:v h264_nvenc " + "\"" + output + "\"";
//            String INTEL_GPU = "\"" + ffmpegPath + "\"" + " -i " + "\"" + videoFile + "\"" + " -i " + "\"" + audioFile + "\"" + " -codec copy -c:v h264_qsv " + "\"" + output + "\"";
//            String SOFTWARE_CPU = "\"" + ffmpegPath + "\"" + " -i " + "\"" + videoFile + "\"" + " -i " + "\"" + audioFile + "\"" + " -codec copy -c:v libx264 " + "\"" + output + "\"";
            String DEFAULT = "\"" + ffmpegPath + "\"" + " -i " + "\"" + videoFile + "\"" + " -i " + "\"" + audioFile + "\"" + " -codec copy " + "\"" + output + "\"";

//            System.out.println(DEFAULT);
            outputFile(DEFAULT, output);
        }

    }
}
