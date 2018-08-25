package com.nian;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PackageWebProject {
    public static void main(String[] args) {
        if ((args == null) || (args.length == 0) || (args.length % 2 != 0)) {
            displayHelp();
        } else {
            String msg = "执行命令:";
            for (String arg : args) {
                msg = msg + arg + "\t";
            }
            System.out.println(msg);

            String projectName = null;
            String projectDir = null;
            String deployDir = null;
            String timeStr = null;
            String targetName = null;

            int i = 0;
            while (i < args.length) {
                String cmd = args[i];
                String cmdValue = args[(i + 1)];
                if ((!cmd.startsWith("-")) || (cmdValue.startsWith("-"))) {
                    System.out.println("输入命令有误!");
                    displayHelp();
                    return;
                }
                cmdValue = cmdValue.trim();

                if ("-p".equals(cmd)) {
                    projectDir = cmdValue;
                } else if ("-c".equals(cmd)) {
                    targetName = cmdValue;
                } else if ("-d".equals(cmd)) {
                    deployDir = cmdValue;
                } else if ("-t".equals(cmd)) {
                    timeStr = cmdValue;
                }
               /* else if ("-n".equals(cmd)) {
                    projectName = cmdValue;
                }*/
                i += 2;
            }

            if (StringUtils.isEmpty(projectDir)) {
                projectDir = readLine("项目的目录:", true);
            }
            if (StringUtils.isEmpty(deployDir)) {
                deployDir = readLine("增量包的输出目录:", true);
            }
            /* if (StringUtils.isEmpty(targetName)) {
                targetName = readLine("项目编译目录:", true);
            }*/
            /*if (StringUtils.isEmpty(projectName)) {
                projectName = readLine("项目的名称:", false);
            }*/
            //如果为空 默认传当天0点的时间
            /*if (StringUtils.isEmpty(timeStr)) {
                timeStr = readLine("增量时间，格式：yyyy/MM/dd-HH:mm", false);
            }*/

            List exList = new ArrayList();
            List needFiles = new ArrayList();
            /*if ("web_wutuojia".equals(projectName)) {
                exList.add(projectDir + "/demo");
                exList.add(projectDir + "/image");
            }
            else if ("wtj-sso".equals(projectName)) {
                exList.add(projectDir + "/src/main/java");
                exList.add(projectDir + "/src/test");
                needFiles.add("wtj-common-1.0-SNAPSHOT.jar");
            }*/
            long time = 0l;
            if (StringUtils.isEmpty(timeStr)) {
                time = PackageMain.getTodayTime();
            } else {
                Date date = PackageMain.parse(timeStr, "yyyy/MM/dd-HH:mm");
                time = date.getTime();
            }

            PackageMain.deploy(projectDir, targetName, time, deployDir, exList, needFiles);
        }
    }

    private static String readLine(String msg, boolean isFile) {
        String projectDir = null;
        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            boolean notGet = true;
            while (notGet) {
                System.out.println("请输入" + msg);
                projectDir = br.readLine();
                if (StringUtils.isNotEmpty(projectDir)) {
                    if (isFile) {
                        if (new File(projectDir).exists()) {
                            notGet = false;
                        }
                    } else {
                        notGet = false;
                    }
                }
            }
            System.out.println(msg + projectDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return projectDir;
    }

    private static void displayHelp() {
        System.out.println("使用帮助:");
        System.out.println("-p 项目文件目录");
        System.out.println("-d 增量包的输出目录");
        System.out.println("-c 项目编译目录(nullable)");
        System.out.println("-n 项目名称(nullable)");
        System.out.println("-t 增量时间(nullable)，格式：yyyy/MM/dd-HH:mm");
    }
}