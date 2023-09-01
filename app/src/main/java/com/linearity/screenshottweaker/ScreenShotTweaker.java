package com.linearity.screenshottweaker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ScreenShotTweaker implements IXposedHookLoadPackage{
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals("com.android.systemui")
        ) {
            try {
                XposedBridge.log("[linearity]trying:"+ loadPackageParam.packageName);
                Class<?> takeScreenshotService =
                        XposedHelpers.findClass(
                            "com.android.systemui.screenshot.TakeScreenshotService",
                                loadPackageParam.classLoader);

                        XposedHelpers.findAndHookMethod(
                                takeScreenshotService,
                                "handleMessage",
                                Message.class,
                                xcMethodReplacement);
//                XposedBridge.log(Arrays.toString(takeScreenshotService.getDeclaredMethods()));
                XposedBridge.log("[linearity]" + "Success!");
            } catch (Exception t) {
                XposedBridge.log(t);
            }
        }

    }

    XC_MethodReplacement xcMethodReplacement = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            XposedBridge.log("[linearity]" + "Replaced:com.android.systemui.screenshot.TakeScreenshotService handleMessage");
//            Context mContext = AndroidAppHelper.currentApplication().getApplicationContext();
//            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//            DisplayMetrics displayMetrics = new DisplayMetrics();
            takeScreenshot();
            return false;
        }
    };

    @SuppressLint("SdCardPath")
    private void takeScreenshot() {
        try{
//            SM2 sm2 = new SM2();

            long currentTime = System.currentTimeMillis();
            String name = SM2EncryptUtils.encrypt(String.valueOf(currentTime));
            File folder = new File("/storage/emulated/0/ScreenShots/");
            if (!folder.exists()){
                folder.mkdirs();
            }
            String filename = "/storage/emulated/0/ScreenShots/" + name + ".png";
            String filenamePNG = "/storage/emulated/0/ScreenShots/" + name + "_compressed.png";
            XposedBridge.log("[linearity]" + "ScreenShot saving: " + filename);
            Process sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            os.write(("/system/bin/screencap -p " + filename).getBytes(StandardCharsets.US_ASCII));
            os.flush();
            os.close();
            sh.waitFor();
            File file1 = new File(filename);
            file1.setWritable(false);
            file1.setLastModified(114514);
            XposedBridge.log("[linearity]" + "ScreenShot saved: " + filename);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filename));
//            byte[] data = bufferedInputStream.toByteArray();
            Thread thread = new Thread(() -> {
                XposedBridge.log("[linearity]" + "ScreenShot Compressing:" + filename);
                int MaxDist = 8;
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream).copy(Bitmap.Config.ARGB_8888, true);
                    for (int i=0;i<bitmap.getWidth();i+=2){
                        for (int j=0;j<bitmap.getHeight();j+=2){
                            CalculateAndProcess(MaxDist, i, j, bitmap, 0, 0);
                        }
                        for (int j=1;j<bitmap.getHeight();j+=2){
                            CalculateAndProcess(MaxDist, i, j, bitmap, 0, 1);
                        }
                    }
                    for (int i=1;i<bitmap.getWidth();i+=2){
                        for (int j=0;j<bitmap.getHeight();j+=2){
                            CalculateAndProcess(MaxDist, i, j, bitmap, 1, 0);
                        }
                        for (int j=1;j<bitmap.getHeight();j+=2){
                            CalculateAndProcess(MaxDist, i, j, bitmap, 1, 1);
                        }
                    }
                    BufferedOutputStream bOutS = new BufferedOutputStream(new FileOutputStream(filenamePNG));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bOutS);
                    bOutS.flush();
                    bOutS.close();
                    File file = new File(filenamePNG);
                    file.setWritable(false);
                    file.setLastModified(114514);
                    file.setReadOnly();
                    OutputStream su = Runtime.getRuntime().exec("su").getOutputStream();
                    su.write(("chmod a-w " + filenamePNG).getBytes(StandardCharsets.US_ASCII));
                    su.flush();
                    su.close();
                    XposedBridge.log("[linearity]" + "ScreenShot Compressed: " + filenamePNG);
                    new File(filename).delete();
                }catch (Exception e){
                    XposedBridge.log(e);
                }
            });
            thread.start();

        }catch (Exception e){
//            XposedBridge.log("[linearity]" + e);
            XposedBridge.log(e);
        }
    }

    //Alert:For ARGB
    //Against LSB
    void CalculateAndProcess(int MaxDist, int i, int j, Bitmap bitmap, int minI, int minJ){
        int thisColor = bitmap.getPixel(i, j);
        int thisColorA = 255;
        int thisColorR = (thisColor & 0x00ff0000) >> 16;
        int thisColorG = (thisColor & 0x0000ff00) >> 8;
        int thisColorB = (thisColor & 0x000000ff);
        Random random = new Random();
        if (random.nextBoolean())
        {
            if (i>minI)
            {
                int targColor = bitmap.getPixel(i-2, j);
//                int targColorA = (targColor & 0xff000000) >> 24;
                int targColorR = (targColor & 0x00ff0000) >> 16;
                int targColorG = (targColor & 0x0000ff00) >> 8;
                int targColorB = (targColor & 0x000000ff);
//                if (Math.abs((thisColorA - targColorA)) <= MaxDist){
//                    thisColorA = targColorA;
//                }
                if (Math.abs((thisColorR - targColorR)) <= MaxDist){
                    thisColorR = targColorR;
                }
                if (Math.abs((thisColorG - targColorG)) <= MaxDist){
                    thisColorG = targColorG;
                }
                if (Math.abs((thisColorB - targColorB)) <= MaxDist){
                    thisColorB = targColorB;
                }
//                thisColorA = Math.min(thisColorA + random.nextInt(MaxDist), 255);
                thisColorR = Math.min(thisColorR + random.nextInt(MaxDist), 255);
                thisColorG = Math.min(thisColorG + random.nextInt(MaxDist), 255);
                thisColorB = Math.min(thisColorB + random.nextInt(MaxDist), 255);
                thisColor = (thisColorA << 24) + (thisColorR << 16) + (thisColorG << 8) + thisColorB;
                bitmap.setPixel(i,j,thisColor);
            }
        }
        else
        {
            if(j>minJ)
            {
                int targColor = bitmap.getPixel(i, j-2);
//                int targColorA = (targColor & 0xff000000) >> 24;
                int targColorR = (targColor & 0x00ff0000) >> 16;
                int targColorG = (targColor & 0x0000ff00) >> 8;
                int targColorB = (targColor & 0x000000ff);
//                if (Math.abs((thisColorA - targColorA)) <= MaxDist){
//                    thisColorA = targColorA;
//                }
                if (Math.abs((thisColorR - targColorR)) <= MaxDist){
                    thisColorR = targColorR;
                }
                if (Math.abs((thisColorG - targColorG)) <= MaxDist){
                    thisColorG = targColorG;
                }
                if (Math.abs((thisColorB - targColorB)) <= MaxDist){
                    thisColorB = targColorB;
                }
//                thisColorA = Math.min(thisColorA + random.nextInt(MaxDist), 255);
                thisColorR = Math.min(thisColorR + random.nextInt(MaxDist), 255);
                thisColorG = Math.min(thisColorG + random.nextInt(MaxDist), 255);
                thisColorB = Math.min(thisColorB + random.nextInt(MaxDist), 255);
                thisColor = (thisColorA << 24) + (thisColorR << 16) + (thisColorG << 8) + thisColorB;
                bitmap.setPixel(i,j,thisColor);
            }
        }
    }
}
