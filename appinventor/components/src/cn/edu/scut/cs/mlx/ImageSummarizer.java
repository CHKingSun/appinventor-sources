package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

import org.json.*;

/**
 *  图像概述
 *  上传jpg格式图像文件到服务器，由服务器生成描述该图像的英文语句。
 */
@DesignerComponent(version = YaVersion.IMAGE_SUMMARIZER_COMPONENT_VERSION,
    description = "ImageSummarizer",
    category = ComponentCategory.AI,
    nonVisible = true,
    iconName = "images/ai.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "json.jar")
@SimpleObject
public class ImageSummarizer extends MLXBase{
    private String uploadedImageName = null;
    private JSONArray resultData = null;
    
    public ImageSummarizer(ComponentContainer container){
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
     *  上传jpg格式的图像到服务器，并令其作为输入。
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
     *  @return 已上传图像的URL。
     */
    @SimpleFunction(description = "GetUploadedImageURL")
    public String GetUploadedImageURL(){
        return (uploadedImageName != null) ? SERVER + "/images/get?filename=" + uploadedImageName : "";
    }
    
    /**
     *  获取生成的描述语句的数量。如果未生成，返回0。
     *  @return 生成的描述语句的数量。
     */
    @SimpleFunction(description = "GetNumSentences")
    public int GetNumSentences(){
        return (resultData != null) ? resultData.length() : 0;
    }
    
    /**
     *  获取指定索引值的描述语句。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 指定索引值的描述语句。
     */
    @SimpleFunction(description = "GetSentence")
    public String GetSentence(int index){
        try{
            if(resultData != null){
                JSONObject sentence = this.resultData.getJSONObject(index - 1);
                return sentence.getString("sentence");
            }
        }catch(Exception e){
            raiseError("GetSentence", -1, "Failed to parse JSON");
        }
        return null;
    }
    
    /**
     *  获取指定索引值的描述语句的分数。
     *  分数为浮点数，范围在(0, 1)之间。
     *  @param index 索引值，合法的索引从1开始。
     *  @return 指定索引值的描述语句的分数。
     */
    @SimpleFunction(description = "GetSentenceScore")
    public double GetSentenceScore(int index){
        try{
            if(resultData != null){
                JSONObject sentence = this.resultData.getJSONObject(index - 1);
                return sentence.getDouble("score");
            }
        }catch(Exception e){
            raiseError("GetSentenceScore", -1, "Failed to parse JSON");
        }
        return 0;
    }
    
    /**
     *  异步请求服务器开始生成描述。服务器返回结果后，触发GotResult事件。
     *  @see GotResult()
     */
    @SimpleFunction(description = "StartGeneration")
    public void StartGeneration(){
        if(uploadedImageName != null){
            get("/im2txt?input=" + uploadedImageName, new AsyncCallbackPair<String>(){
                public void onSuccess(String result){
                    try{
                        ImageSummarizer.this.resultData = new JSONArray(result);
                        ImageSummarizer.this.GotResult();
                    }catch(Exception e){
                        raiseError("GotResult", -1, "Failed to parse JSON");
                    }
                }
                public void onFailure(String msg){}
            });
        }
    }
    
    /**
     *  获得生成结果事件。接收到服务器返回的生成结果时触发此事件。
     */
    @SimpleEvent(description = "GotResult")
    public void GotResult(){
        EventDispatcher.dispatchEvent(getInstance(), "GotResult");
    }
}