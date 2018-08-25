package com.nian;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PackageMain {
    private static final String dateFMT = "yyyy-MM-dd HH:mm:ss";

    private static final String javaDirPath = "/src/main/java";
    private static final String resourceDirPath = "/src/main/resources";
    private static final String webappPath = "/src/main/webapp";
    private static final String classPath = "/WEB-INF/classes";

    public static void main(String[] args) {
//        String projectDir = "/Users/hotelwang/data/workspace/tongfeng/energy_web";
        String projectDir = "/Users/hotelwang/data/workspace/ycc/bluetooth_web/WebRoot";
        String deployDir = "/Users/hotelwang/Desktop/output";
        Date date = parse("2018-05-21 00:00:00", "yyyy-MM-dd HH:mm:ss");
        long time = date.getTime();
        String targetName = null;
        List exList = new ArrayList();
        List needFiles = new ArrayList();
//        needFiles.add("wtj-common-1.0-SNAPSHOT.jar");

        deploy(projectDir, targetName, time, deployDir, exList, needFiles);
    }

    public static void deploy(String projectDir, String targetName, long time, String deployDir, List<String> exList,
                              List<String> needFiles) {
        File pomFile = new File(projectDir + "/pom.xml");
        boolean isMaven = false;
        if (pomFile.exists()) {
            isMaven = true;
        }

        String projectName = projectDir.substring(projectDir.lastIndexOf("/") + 1, projectDir.length());
        if (!isMaven) {
            if (projectDir.endsWith("/")) {
                projectDir = projectDir.substring(0, projectDir.length() - 1);
            }
            projectName = projectDir.substring(0, projectDir.lastIndexOf("/"));//要么以/WebRoot结尾或者已/WebContent结尾
            projectName = projectName.substring(projectName.lastIndexOf("/") + 1, projectName.length());
        }
        String msg = "===项目:" + projectName + "增量打包，日期：" + toDateString(time) + "===\n";
        msg = msg + "===项目的目录:" + projectDir + "===\n";
        msg = msg + "===项目的编译目录:" + targetName + "===\n";
        msg = msg + "===增量打包的存放目录:" + deployDir + "===\n";
        msg = msg + "===必须增量的文件:" + needFiles + "===\n";
        msg = msg + "===可以忽略增量的文件:" + exList + "===\n";
        if ("test".endsWith(projectName)) {
            return;
        }

        if (isMaven) {
            msg = "Maven" + msg;
        }
        System.out.println(msg);
        String deployPath = deployDir + "/" + projectName;
        try {
            File deployFile = new File(deployPath);
            if (!deployFile.exists()) {
                deployFile.mkdirs();
            } else {
                FileUtils.deleteDirectory(deployFile);
                System.out.println("删除上一次的打包文件:" + deployFile.getAbsolutePath());
            }

            File srcDir = new File(projectDir);
            if (isMaven) {
                new File(projectDir + "/src");
            }
            File javaDir = new File(projectDir + javaDirPath);
            File resourceDir = new File(projectDir + resourceDirPath);
            File webappDir = new File(projectDir);
            if (isMaven) {
                webappDir = new File(projectDir + webappPath);
            }

            String javaDirName = javaDir.getAbsolutePath();
            String resourceDirName = resourceDir.getAbsolutePath();
            String webappDirName = webappDir.getAbsolutePath();

            List<String> list = new ArrayList<String>();
            List<String> newFiles = listNewFile(srcDir, time, exList);
            for (String newFile : newFiles) {
                String realName = "";
                if (newFile.endsWith(".DS_Store")) {
                    continue;
                }
                if (newFile.startsWith(javaDirName)) {
                    realName = newFile.replace(javaDirName, "");
                    realName = "/WEB-INF/classes" + realName.substring(0, realName.lastIndexOf(".")) + ".class";
                } else if (newFile.startsWith(resourceDirName)) {
                    realName = "/WEB-INF/classes" + newFile.replace(resourceDirName, "");
                } else if (newFile.startsWith(webappDirName)) {
                    realName = newFile.replace(webappDirName, "");
                } else if (!isMaven) {
                    realName = newFile.replace(webappDirName, "");
                }

                if ((StringUtils.isNotEmpty(realName)) && (realName.indexOf("WEB-INF/classes") < 0)) {
                    realName = deployPath + realName.replace("\\", "/");
                    list.add(realName);
                }
                //把class目录下的配置文件也加进去
                if ((StringUtils.isNotEmpty(realName)) && (realName.indexOf("WEB-INF/classes") > 0) && !realName.endsWith(".class")) {
                    realName = deployPath + realName.replace("\\", "/");
                    list.add(realName);
                }
            }

            System.out.println("需要增量的文件数量:" + list.size());

            if (targetName == null) {//maven web
                targetName = projectDir + "/target/" + projectName;
            }
            File targetFile = new File(targetName);
            if (!targetFile.exists()) {//normal web
                targetName = projectDir;
                targetFile = new File(targetName);
            }
            dealTargetFile(targetFile, list, deployPath, targetName, needFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dealTargetFile(File dir, List<String> list, String deployPath, String targetName,
                                      List<String> needFiles) {
        try {
            File[] fs = dir.listFiles();
            for (int i = 0; i < fs.length; i++) {
                File f = fs[i];
                String fName = f.getAbsolutePath();
                //test
                String testPath = "/Users/hotelwang/data/workspace/tongfeng/energy_web/src/main/webapp/WEB-INF/pages/equipment/station.jsp";
                if (testPath.equals(fName)) {
                    System.out.println(1);
                }
                fName = fName.replace("\\", "/");
                fName = fName.replace(targetName, "");
                fName = fName.replace(javaDirPath, "");
                fName = fName.replace(resourceDirPath, "");
                fName = fName.replace(webappPath, "");
                if (f.isDirectory()) {
                    String clsPath = File.separator + "WEB-INF" + File.separator + "classes";
                    if (!fName.endsWith(".svn")) {
                        if (f.getAbsolutePath().endsWith(clsPath)) {
                            //如果是classes目录下面的文件有可能是配置文件，不用复制
                            for (File file : f.listFiles()) {
                                String deployDir = deployPath + clsPath;
                                deployDir = deployDir.replace("\\", "/");
                                String outputPath = deployDir + File.separator + file.getName();
                                if (file.isDirectory()) {
                                    System.out.println("复制所有Class: " + file.getAbsolutePath() + " --> " + outputPath);
                                    FileUtils.copyDirectory(file, new File(outputPath));
                                } else {
                                    //如果是文件，是class文件直接拷贝过去
                                    if (file.getAbsolutePath().endsWith(".class")) {
                                        System.out.println("复制所有Class: " + file.getAbsolutePath() + " --> " + outputPath);
                                        FileUtils.copyFile(file, new File(outputPath));
                                    } else {//如果是配置文件，更新日期之后的才拷贝过去
                                        copyFile(file, list, deployPath, targetName, needFiles);

                                    }
                                }
                            }
                        } else {
                            dealTargetFile(f, list, deployPath, targetName, needFiles);
                        }
                    }
                } else {
                    copyFile(f, list, deployPath, targetName,
                            needFiles);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //拷贝文件
    private static void copyFile(File file, List<String> list, String deployPath, String targetName,
                                 List<String> needFiles) throws IOException {
        String deployDir = deployPath;
        String fName = file.getAbsolutePath();
        fName = fName.replace("\\", "/");
        fName = fName.replace(targetName, "");
        fName = fName.replace(javaDirPath, "");
        fName = fName.replace(resourceDirPath, "");
        fName = fName.replace(webappPath, "");
        int index = fName.indexOf(classPath);
        if(index!=-1){
            fName=fName.substring(index);
        }
        fName = deployDir + fName;
        fName = fName.replace("\\", "/");

        for (String needFile : needFiles) {
            if (file.getAbsolutePath().endsWith(needFile)) {
                System.out.println("必须增量的文件：" + fName);
                FileUtils.copyFile(file, new File(fName));
                return;
            }
        }

        if (list.contains(fName)) {
            System.out.println("增量文件：" + fName);
            FileUtils.copyFile(file, new File(fName));
        }
    }

    public static List<String> listNewFile(File dir, long time, List<String> exList) {
        List list = new ArrayList();
        readNewFiles(dir, time, list, exList);
        return list;
    }

    public static void readNewFiles(File dir, long time, List<String> list, List<String> exList) {
        try {
            if (!dir.exists()) {
                return;
            }
            File[] fs = dir.listFiles();
            for (int i = 0; i < fs.length; i++) {
                File f = fs[i];
                if (f.isDirectory()) {
                    String fName = f.getAbsolutePath();
                    fName = fName.replace("\\", "/");
                    if (!exList.contains(fName))
                        readNewFiles(f, time, list, exList);
                    else if (fName.endsWith(".svn"))
                        System.out.println("不包含svn:  " + fName);
                    else
                        System.out.println("不包含:  " + fName);
                } else {
                    String fileName = f.getAbsolutePath();
                    long mTime = f.lastModified();
                    if (mTime >= time)
                        list.add(fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Date parse(String dateStr, String fmt) {
        DateFormat formatter = new SimpleDateFormat(fmt);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toDateString(Date date, String fmt) {
        DateFormat formatter = new SimpleDateFormat(fmt);
        return formatter.format(date);
    }

    public static String toDateString(long time) {
        return toDateString(new Date(time), "yyyy-MM-dd HH:mm:ss");
    }

    public static long getTodayTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime().getTime();
    }
}