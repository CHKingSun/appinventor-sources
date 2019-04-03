package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

import org.json.*;

/**
 *  人脸表情与性别识别
 *  上传jpg格式图像文件到服务器，由服务器计算并返回人脸的包围盒、表情、性别等信息。
 */
@DesignerComponent(version = YaVersion.EMOTION_AND_GENDER_COMPONENT_VERSION,
    description = "EmotionAndGender",
    category = ComponentCategory.AI,
    nonVisible = true,
    iconName = "images/ai.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "json.jar")
@SimpleObject
public class EmotionAndGender extends MLXBase{
    private String uploadedImageName = null;
    private String resultImageName = null;
    private JSONArray resultData = null;
    
    public EmotionAndGender(ComponentContainer container){
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
     *  上传jpg格式的图像到服务器，并令其作为检测的输入。
     *  注意图像文件的格式为jpg，且大小不超过5MB。
     *  @param path 文件的路径
     */
    @SimpleFunction(description = "UploadAndUseImage")
    public void UploadAndUseImage(String path){
        uploadedImageName = null;
        resultImageName = null;
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
     *  获取检测结果图像的URL。检测结果图像中含有人脸的包围框，表情，性别等信息。
     *  @return 检测结果图像的URL
     */
    @SimpleFunction(description = "GetResultImageURL")
    public String GetResultImageURL(){
        return (resultImageName != null) ? SERVER + "/images/get?filename=" + resultImageName : "";
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
     *  @param index 索引值，合法的索引从1开始。
     *  @return 一个列表，包含指定索引的人脸包围盒坐标。
     */
    @SimpleFunction(description = "GetDetectionBox")
    public Object GetDetectionBox(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                JSONArray box = detection.getJSONArray("face_coords");
                return JsonUtil.getListFromJsonArray(box);
            }
        }catch(Exception e){
            raiseError("GetDetectionBox", -1, "Failed to parse JSON");
        }
        return null;
    }
    
    /**
     *  获取指定索引的人脸的性别。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 性别("man"或"woman")。
     */
    @SimpleFunction(description = "GetDetectionGender")
    public String GetDetectionGender(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                return detection.getString("gender");
            }
        }catch(Exception e){
            raiseError("GetDetectionGender", -1, "Failed to parse JSON");
        }
        return null;
    }
    
    /**
     *  获取指定索引的人脸的表情。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 表情("neutral", "happy", "sad", "suprise", "fear")。
     */
    @SimpleFunction(description = "GetDetectionEmotion")
    public String GetDetectionEmotion(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                return detection.getString("emotion");
            }
        }catch(Exception e){
            raiseError("GetDetectionEmotion", -1, "Failed to parse JSON");
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
            get("/emotion_and_gender?input=" + uploadedImageName, new AsyncCallbackPair<String>(){
                public void onSuccess(String result){
                    try{
                        JSONObject json = new JSONObject(result);
                        EmotionAndGender.this.resultImageName = json.getString("img");
                        EmotionAndGender.this.resultData = json.getJSONArray("data");
                        EmotionAndGender.this.GotResult();
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