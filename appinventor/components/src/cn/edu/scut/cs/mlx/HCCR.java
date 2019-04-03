package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

/**
 *  汉字手写体识别
 *  上传jpg格式图像文件到服务器，由服务器识别并返回图像中的汉字。
 */
@DesignerComponent(version = YaVersion.HCCR_COMPONENT_VERSION,
    description = "HandwrittenChineseCharacterRecognizer",
    category = ComponentCategory.AI,
    nonVisible = true,
    iconName = "images/ai.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")
@SimpleObject
public class HCCR extends MLXBase{
    private String uploadedImageName = null;
    private String results[] = null;
    
    public HCCR(ComponentContainer container){
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
        results = null;
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
     *  获取识别结果(汉字)的列表。
     *  @return 识别结果(汉字)的列表。
     */
    @SimpleFunction(description = "GetResultCharacters")
    public Object GetResultCharacters(){
        return (results != null) ? YailList.makeList(results) : YailList.makeEmptyList();
    }
    
    /**
     *  异步请求服务器开始物体识别。服务器返回结果后，触发GotResult事件。
     *  @see GotResult()
     */
    @SimpleFunction(description = "StartRecognition")
    public void StartRecognition(){
        if(uploadedImageName != null){
            get("/hccr?input=" + uploadedImageName, new AsyncCallbackPair<String>(){
                public void onSuccess(String result){
                    String str[] = result.split("");
                    results = new String[str.length-1];
                    for(int i=1;i<str.length;i++)
                        results[i-1] = str[i];
                    HCCR.this.GotResult();
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