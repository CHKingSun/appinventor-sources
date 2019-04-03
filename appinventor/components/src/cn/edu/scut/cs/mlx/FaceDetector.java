package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

import org.json.*;

/**
 *  人脸识别
 *  上传jpg格式图像文件到服务器，由服务器计算并返回人脸的包围盒、器官坐标等信息。
 */
@DesignerComponent(version = YaVersion.FACE_DETECTOR_COMPONENT_VERSION,
        description = "FaceDetector",
        category = ComponentCategory.AI,
        nonVisible = true,
        iconName = "images/ai.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "json.jar")
@SimpleObject
public class FaceDetector extends MLXBase{
    private String uploadedImageName = null;
    private JSONArray resultData = null;

    public FaceDetector(ComponentContainer container){
        super(container);
    }
    
    /**
     *  获取服务器地址。
     *  @return 服务器地址
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String Server() {
        return SERVER;
    }

    /**
     *  设置服务器地址。
     *  @param url 服务器地址，形如http://[domain]，或http://[ipAddr]:[port]
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty
    public void Server(String url) {
        SERVER = url;
    }
    
    /**
     *  获取服务器返回的未经处理的JSON信息。
     *  @return JSON信息。
     */
    @SimpleFunction(description = "RawJSONData")
    public String RawJSONData(){
        return (resultData != null) ? resultData.toString() : "";
    }

    /**
     *  上传jpg格式的图像到服务器，并令其作为检测的输入。
     *  注意图像文件的格式为jpg，且大小不超过5MB。
     *  @param path 文件的路径
     */
    @SimpleFunction(description = "UploadAndUseImage")
    public void UploadAndUseImage(String path){
        uploadedImageName = null;
        resultData = null;
        uploadImage(path, new AsyncCallbackPair<String>(){
            public void onSuccess(String result){
                uploadedImageName = result;
                ImageUploadFinished();
            }
            public void onFailure(String msg){
                uploadedImageName = null;
                ImageUploadFinished();
            }
        });
    }

    /**
     *  图像上传完成事件。
     *  可以用GetUploadedImageURL()获取已上传图像的URL。如果URL不为空，则上传成功，否则上传失败。
     *  @see GetUploadedImageURL()
     */
    @SimpleEvent(description = "ImageUploadFinished")
    public void ImageUploadFinished(){
        EventDispatcher.dispatchEvent(getInstance(), "ImageUploadFinished");
    }

    /**
     *  获取已上传图像的URL。如果URL不为空，则上传成功，否则上传失败。
     *  @return 已上传图像的URL
     */
    @SimpleFunction(description = "GetUploadedImageURL")
    public String GetUploadedImageURL(){
        return (uploadedImageName != null) ? SERVER + "/images/get?filename=" + uploadedImageName : "";
    }

    /**
     *  获取检测到的人脸数量
     *  @return 检测到的人脸数量，未进行检测或检测失败则为0。
     */
    @SimpleFunction(description = "GetNumDetections")
    public int GetNumDetections(){
        return (resultData != null) ? resultData.length() : 0;
    }

    /**
     *  获取指定索引的人脸包围盒坐标列表(xmin, ymin, xmax, ymax)，对应其左上角和右下角坐标。
     *  坐标范围在[0, 1]之间。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 一个列表，包含指定索引的人脸包围盒坐标。
     */
    @SimpleFunction(description = "GetDetectionBox")
    public Object GetDetectionBox(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                JSONArray box = detection.getJSONArray("location");
                return JsonUtil.getListFromJsonArray(box);
            }
        }catch(Exception e){
            raiseError("GetDetectionBox", -1, "Failed to parse JSON");
        }
        return null;
    }

    /**
     *  获取指定索引的人脸的128维向量编码。可以用余弦相似度计算人脸相似度。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 一个列表，包含128个数值。
     */
    @SimpleFunction(description = "GetDetectionEncoding")
    public Object GetDetectionEncoding(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                JSONArray encoding = detection.getJSONArray("encoding");
                return JsonUtil.getListFromJsonArray(encoding);
            }
        }catch(Exception e){
            raiseError("GetDetectionEncoding", -1, "Failed to parse JSON");
        }
        return null;
    }

    /**
     *  获取指定索引的人脸的器官名称列表。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 器官名称列表。
     */
    @SimpleFunction(description = "GetDetectionLandmarkNames")
    public Object GetDetectionLandmarkNames(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                JSONObject landmark = detection.getJSONObject("landmark");
                return JsonUtil.getListFromJsonArray(landmark.names());
            }
        }catch(Exception e){
            raiseError("GetDetectionLandmarkNames", -1, "Failed to parse JSON");
        }
        return null;
    }
    
    /**
     *  获取指定索引的人脸中，指定名称的器官的坐标列表。
     *  坐标列表形如((x1, y1), (x2, y2), ..., (xn, yn)), 坐标范围在[0, 1]之间。
     *  @param index 索引值，合法的索引从1开始。
     *  @param name 器官名称。
     *  @return 器官坐标列表。
     */
    @SimpleFunction(description = "GetDetectionLandmarkPositions")
    public Object GetDetectionLandmarkPositions(int index, String name){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                JSONObject landmark = detection.getJSONObject("landmark");
                JSONArray positions = landmark.getJSONArray(name);
                
                Object results[] = new Object[positions.length()];
                for(int i=0;i<positions.length();i++){
                    JSONArray point = positions.getJSONArray(i);
                    results[i] = YailList.makeList(JsonUtil.getListFromJsonArray(point));
                }
                return YailList.makeList(results);
            }
        }catch(Exception e){
            raiseError("GetDetectionLandmarkPositions", -1, "Failed to parse JSON");
        }
        return null;
    }

    /**
     *  异步请求服务器开始人脸识别。服务器返回结果后，触发GotResult事件。
     *  @see GotResult()
     */
    @SimpleFunction(description = "StartDetection")
    public void StartDetection(){
        if(uploadedImageName != null){
            get("/face_detection?input=" + uploadedImageName, new AsyncCallbackPair<String>(){
                public void onSuccess(String result){
                    try{
                        FaceDetector.this.resultData = new JSONArray(result);
                        FaceDetector.this.GotResult();
                    }catch(Exception e){
                        raiseError("GotResult", -1, "Failed to parse JSON");
                    }
                }
                public void onFailure(String msg){}
            });
        }
    }

    /**
     *  获得识别结果事件。接收到服务器返回的检测结果时触发此事件。
     */
    @SimpleEvent(description = "GotResult")
    public void GotResult(){
        EventDispatcher.dispatchEvent(getInstance(), "GotResult");
    }
}