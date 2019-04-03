package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

import java.util.*;

/**
 *  图像风格转换
 *  上传jpg格式图像文件到服务器，指定风格样式，由服务器生成混合风格的图像。
 */
@DesignerComponent(version = YaVersion.STYLE_MIXER_COMPONENT_VERSION,
    description = "StyleMixer",
    category = ComponentCategory.AI,
    nonVisible = true,
    iconName = "images/ai.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")
@SimpleObject
public class StyleMixer extends MLXBase{
    static final List<String> SUPPORTED_STYLES = Arrays.asList(new String[]{"Robert_Delaunay,_1906,_Portrait",
                                                                            "candy",
                                                                            "composition_vii",
                                                                            "escher_sphere",
                                                                            "feathers",
                                                                            "frida_kahlo",
                                                                            "la_muse",
                                                                            "mosaic",
                                                                            "mosaic_ducks_massimo",
                                                                            "pencil",
                                                                            "picasso_selfport1907",
                                                                            "rain_princess",
                                                                            "seated-nude",
                                                                            "shipwreck",
                                                                            "starry_night",
                                                                            "stars2",
                                                                            "strip",
                                                                            "the_scream",
                                                                            "udnie",
                                                                            "wave",
                                                                            "woman-with-hat-matisse"});
    private String uploadedImageName = null;
    private String resultImageName = null;
    private String style = "candy";
    
    public StyleMixer(ComponentContainer container){
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
        resultImageName = null;
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
     *  获取可用风格样式的列表。
     *  @return 可用风格样式的列表。
     */
    @SimpleFunction(description = "GetSupportedStyles")
    public static Object GetSupportedStyles(){
        return YailList.makeList(SUPPORTED_STYLES);
    }
    
    /**
     *  获取当前风格样式。
     *  默认值为candy。
     *  @return 当前风格样式。
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String Style(){
        return this.style;
    }
    
    /**
     *  设置当前风格样式。
     *  @param style 风格样式。
     *  @throws YailRuntimeError 如果给定风格样式不受支持。
     *  @see GetSupportedStyles()
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
                        defaultValue = "candy")
    @SimpleProperty
    public void Style(String style){
        if(SUPPORTED_STYLES.contains(style))
            this.style = style;
        else
            throw new YailRuntimeError("Style unsupported, consult GetSupportedStyles() for a list of supported style strings.", "Style Unsupported");
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
     *  获取转换结果图像的URL。
     *  如未进行转换或转换失败则返回空字符串。
     *  @return 检测结果图像的URL。
     */
    @SimpleFunction(description = "GetResultImageURL")
    public String GetResultImageURL(){
        return (resultImageName != null) ? SERVER + "/images/get?filename=" + resultImageName : "";
    }
    
    /**
     *  异步请求服务器开始风格转换。服务器返回结果后，触发GotResult事件。
     *  @see GotResult()
     */
    @SimpleFunction(description = "StartTransfer")
    public void StartTransfer(){
        if(uploadedImageName != null){
            get("/style_transfer?input=" + uploadedImageName + "&style=" + urlEncode(Style()), new AsyncCallbackPair<String>(){
                public void onSuccess(String result){
                    StyleMixer.this.resultImageName = result;
                    StyleMixer.this.GotResult();
                }
                public void onFailure(String msg){}
            });
        }
    }
    
    /**
     *  获得转换结果事件。接收到服务器返回的转换结果时触发此事件。
     */
    @SimpleEvent(description = "GotResult")
    public void GotResult(){
        EventDispatcher.dispatchEvent(getInstance(), "GotResult");
    }
}