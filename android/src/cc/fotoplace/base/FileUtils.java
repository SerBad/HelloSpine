package cc.fotoplace.base;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by JayChou on 2019/2/21.
 */
public class FileUtils {

    //保存图片文件
    public static String saveToFile(String fileFolderStr, Bitmap croppedImage) throws FileNotFoundException, IOException {
        File jpgFile = new File(fileFolderStr);
        if (!jpgFile.getParentFile().exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
            mkdir(jpgFile.getParentFile());
        }
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
        croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
        return jpgFile.getPath();
    }

    public static boolean mkdir(File file) {
        while (!file.getParentFile().exists()) {
            mkdir(file.getParentFile());
        }
        return file.mkdir();
    }
}
