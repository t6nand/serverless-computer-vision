package main.java.utils;

import main.java.core.imageprocess.ImageProcessor;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * This class provides all the file utility methods.
 * <p/>
 * Created by tapansharma on 12/10/17.
 */
public class FileUtils {

    private static String getFileBaseName(String path) {
        if (path == null)
            return null;
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }

    /**
     * This method returns the local destination path for the image url.
     *
     * @param imageUrl The S3 image url.
     * @return The local destination path to be used.
     */
    private static String getLocalDestinationPath(String imageUrl) {
        // Extract Image file name
        String fileName = getFileBaseName(imageUrl);

        // File download Path: "s3media/filename"
        return "/tmp" + fileName;
    }

    /**
     * @param sourceFilePath      The path to the source file
     * @param destinationFilePath The path to the destination file
     * @throws Exception
     */
    public static void copyFile(String sourceFilePath, String destinationFilePath) throws Exception {
        File srcFile = new File(sourceFilePath);
        File destFile = new File(destinationFilePath);
        try {
            org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            throw new Exception("Unable to copy files ", e);
        }
    }

    public static void copyFileReplaceExisting(String sourcePath, String destinationPath) throws Exception {
        System.out.println("Copying " + sourcePath + " to " + destinationPath);
        Path from = new File(sourcePath).toPath();
        Path to = new File(destinationPath).toPath();
        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("Unable to copy files ", e);
        }
    }

    public static boolean copyFile(File source, File dest) {
        try {
            IOUtils.copy(new FileInputStream(source), new FileOutputStream(dest));
        } catch (Exception e) {
            System.out.println("Exception in copying files " + e);
            return false;
        }

        return true;
    }

    /**
     * This function loads an image from url and saves into disk. Then it auto-orients the image using imagemagick
     * and reloads using Opencv.
     *
     * @param imageUrl The url of the image.
     * @return The Mat object of the auto-oriented image.
     * @throws Exception
     */
    public static Mat loadMatImageFromUrl(String imageUrl) throws Exception {
        boolean imgOriented = false;

        // File download Path: s3Media/tmp/filename
        String destinationPath = getLocalDestinationPath(imageUrl);

        // Download Image to Disk
        saveImageFromURL(imageUrl, destinationPath);

        // Auto-Orient Image. If a file has exif metadata - "Orientation set to any value 2-8, re-orient it."
        // ToDo: Check if auto-orientation is needed as we have already moved to opencv 3.1 which resolves the
        // orientation issue.
        if (ImageProcessor.shouldAutoOrientImage(ImageProcessor.imageOrientationValue(destinationPath))) {
            System.out.println("Reorienting Image: " + imageUrl);
            boolean succeeded = ImageProcessor.autoOrientImage(destinationPath, destinationPath);
            if (succeeded) {
                imgOriented = true;
                System.out.println("Image Reoriented: " + imageUrl);
            } else {
                System.out.println("Failed to auto-orient an image, url is: " + imageUrl);
                throw new Exception("Failed to auto-orient an image, url is: " + imageUrl);
            }
        }

        // Load the image
        Mat mat = Imgcodecs.imread(destinationPath);

        // Delete the loaded image
        deleteFile(destinationPath);

        // Check if image loaded properly
        if (mat.dataAddr() == 0) {
            String orientedLogStr = "Image loaded from disk is empty, url is: " + imageUrl;
            if (imgOriented) {
                orientedLogStr = "Reoriented " + orientedLogStr;
            }
            System.out.println(orientedLogStr);
            throw new Exception(orientedLogStr);
        }

        return mat;
    }

    public static void saveImageFromURL(String imageUrl, String destinationFile) throws Exception {
        try {
            URL url = new URL(imageUrl);
            saveImageFromURL(url, destinationFile);
        } catch (MalformedURLException e) {
            throw new Exception(e);
        }
    }

    public static void saveImageFromURL(URL imageUrl, String destinationFile) throws Exception {
        try {
            InputStream is = imageUrl.openStream();
            OutputStream os = new FileOutputStream(destinationFile);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * This function downloads an image from a url and saves into disk and returns it File handle.
     *
     * @param imageUrl The url of the image.
     * @return The File object loaded from the downloaded image.
     * @throws Exception
     */
    public static File loadImageFromUrl(String imageUrl) throws Exception {
        // File download Path: "s3Image/filename"
        String destinationPath = getLocalDestinationPath(imageUrl);

        // Download Image to Disk
        saveImageFromURL(imageUrl, destinationPath);

        return new File(destinationPath);
    }

    public static List<File> getVideoFilesInDir(String inputDir)
            throws Exception {
        File inputDirFile = new File(inputDir);
        if (!inputDirFile.isDirectory()) {
            throw new Exception("Incorrect argument, path is not a directory.");
        }

        File[] listOfFiles = inputDirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp4") || name.endsWith(".webm");
            }
        });

        return (listOfFiles != null) ? new ArrayList<>(Arrays.asList(listOfFiles)) : null;
    }

    public static List<File> getImageFilesInDir(String inputDir)
            throws Exception {
        File inputDirFile = new File(inputDir);
        if (!inputDirFile.isDirectory()) {
            throw new Exception("Incorrect argument, path is not a directory.");
        }

        File[] listOfFiles = inputDirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lowerCaseName = name.toLowerCase();
                return lowerCaseName.endsWith(".jpeg") ||
                        lowerCaseName.endsWith(".png") ||
                        lowerCaseName.endsWith(".webp") ||
                        lowerCaseName.endsWith(".gif");
            }
        });

        return (listOfFiles != null) ? new ArrayList<>(Arrays.asList(listOfFiles)) : null;
    }

    /**
     * This method creates a directory path. Any parent directories, if does not exist, will be created.
     *
     * @param dirPath The directory path
     * @throws Exception throws if directory fails to create.
     */
    public static void createDirectory(String dirPath) throws Exception {
        File theDir = new File(dirPath);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("Creating directory: " + dirPath);
            try {
                theDir.mkdirs();
            } catch (SecurityException se) {
                System.out.println("Unable to create Dir: " + dirPath + ", check if the process owner has the directory write access. " +
                        "Exception: " + se.getMessage());
                throw new Exception("Unable to create Dir: " + dirPath, se);
            }

            System.out.println("Directory " + dirPath + " created");
        } else {
            System.out.println("The directory " + dirPath + " already exists.");
        }
    }

    /**
     * This function cleans up any local downloaded files.
     */
    public static void cleanUp(Set<String> localFilesDownloaded) {
        for (String filePath : localFilesDownloaded) {
            FileUtils.deleteFileandDirectory(filePath);
        }
    }

    /**
     * This methods deletes a file using its File handle.
     *
     * @param file The File object
     * @return true if file is deleted.
     */
    public static boolean deleteFile(File file) {
        boolean deleteFlag = false;
        try {
            deleteFlag = file.delete();
            if (!deleteFlag) {
                System.out.println("Failed to delete the image after loading: " + file.getAbsolutePath());
            }
        } catch (SecurityException e) {
            System.out.println("Failed to delete the image after loading: " + file.getAbsolutePath() + ", " + e);
        }
        return deleteFlag;
    }

    public static boolean deleteFile(String filepath) {
        return filepath == null || deleteFile(new File(filepath));
    }

    public static void deleteFileandDirectory(String filepath) {
        if (filepath == null) {
            System.out.println("FORCE_DELETE_FILE: NULL path provided.");
            return;
        }

        try {
            org.apache.commons.io.FileUtils.forceDelete(new File(filepath));
        } catch (Exception e) {
            System.out.println("FORCE_DELETE_FILE: Failed to delete directory/file due to" + e);
        }
    }
}
