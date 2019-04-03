package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

import org.json.*;

/**
 *  物体识别
 *  上传jpg格式图像文件到服务器，由服务器计算并返回物体的位置、类别、置信度等信息。
 */
@DesignerComponent(version = YaVersion.COMMON_OBJECT_SENSOR_COMPONENT_VERSION,
    description = "CommonObjectSensor",
    category = ComponentCategory.AI,
    nonVisible = true,
    iconName = "images/ai.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "json.jar")
@SimpleObject
public class CommonObjectSensor extends MLXBase{
    private String uploadedImageName = null;
    private String resultImageName = null;
    private JSONArray resultData = null;
    
    public CommonObjectSensor(ComponentContainer container){
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
     *  获取检测结果图像的URL。检测结果图像中含有物体的标记框，类别名，置信度等信息。
     *  @return 检测结果图像的URL
     */
    @SimpleFunction(description = "GetResultImageURL")
    public String GetResultImageURL(){
        return (resultImageName != null) ? SERVER + "/images/get?filename=" + resultImageName : "";
    }
    
    /**
     *  获取检测到的物体数量
     *  @return 检测到的物体数量，未进行检测或检测失败则为0。
     */
    @SimpleFunction(description = "GetNumDetections")
    public int GetNumDetections(){
        return (resultData != null) ? resultData.length() : 0;
    }
    
    /**
     *  获取指定索引的物体包围盒坐标列表(xmin, ymin, xmax, ymax)，对应其左上角和右下角坐标。
     *  坐标范围在[0, 1]之间。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 一个列表，包含指定索引的物体包围盒坐标。
     */
    @SimpleFunction(description = "GetDetectionBox")
    public Object GetDetectionBox(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                JSONArray box = detection.getJSONArray("box");
                return JsonUtil.getListFromJsonArray(box);
            }
        }catch(Exception e){
            raiseError("GetDetectionBox", -1, "Failed to parse JSON");
        }
        return null;
    }
    
    /**
     *  获取指定索引的物体的类别名
     *  @param index 索引值，合法的索引从1开始。
     *  @return 指定索引的物体的类别名。
     */
    @SimpleFunction(description = "GetDetectionClassName")
    public String GetDetectionClassName(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                return detection.getString("class_name");
            }
        }catch(Exception e){
            raiseError("GetDetectionClassName", -1, "Failed to parse JSON");
        }
        return null;
    }
    
    /**
     *  获取指定索引的物体的置信度/分数/可能性，为浮点数，范围在(0, 1)
     *  @param index 索引值，合法的索引从1开始。
     *  @return 获取指定索引的物体的置信度。
     */
    @SimpleFunction(description = "GetDetectionScore")
    public double GetDetectionScore(int index){
        try{
            if(resultData != null){
                JSONObject detection = this.resultData.getJSONObject(index - 1);
                return detection.getDouble("score");
            }
        }catch(Exception e){
            raiseError("GetDetectionClassName", -1, "Failed to parse JSON");
        }
        return 0;
    }
    
    /**
     *  异步请求服务器开始物体识别。服务器返回结果后，触发GotResult事件。
     *  @see GotResult()
     */
    @SimpleFunction(description = "StartDetection")
    public void StartDetection(){
        if(uploadedImageName != null){
            get("/object_detection?input=" + uploadedImageName, new AsyncCallbackPair<String>(){
                public void onSuccess(String result){
                    try{
                        JSONObject json = new JSONObject(result);
                        CommonObjectSensor.this.resultImageName = json.getString("img");
                        CommonObjectSensor.this.resultData = json.getJSONArray("data");
                        CommonObjectSensor.this.GotResult();
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