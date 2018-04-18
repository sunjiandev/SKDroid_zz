package com.sunkaisens.skdroid.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.sunkaisens.skdroid.model.ModelPush;

public class XmlDoc {
	public static List<ModelPush> getPushList(String xmlDoc) {
		List<ModelPush> pushList = new ArrayList<ModelPush>();
		// 创建一个新的字符串
		StringReader read = new StringReader(xmlDoc);
		// 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
		InputSource source = new InputSource(read);
		// 创建一个新的SAXBuilder
		SAXBuilder sb = new SAXBuilder();
		try {
			// 通过输入源构造一个Document
			Document doc = sb.build(source);
			// 取的根元素
			Element root = doc.getRootElement();
			System.out.println(root.getName()); // 输出根元素的名称（测试）
			// 得到根元素所有子元素的集合
			List jiedian = root.getChildren();
			// 获得XML中的命名空间（XML中未定义可不写）
			Namespace ns = root.getNamespace();
			Element et = null;
			for (int i = 0; i < jiedian.size(); i++) {
				et = (Element) jiedian.get(i); // 循环依次得到子元素
				
				ModelPush modelPush = new ModelPush();
				
				modelPush.serviceType = et.getChild("serviceType", ns).getText().trim();
				modelPush.msgType = et.getChild("msgType", ns).getText().trim();
				modelPush.id = et.getChild("id", ns).getText().trim();
				modelPush.title = et.getChild("title", ns).getText().trim();
				modelPush.digest = et.getChild("digest", ns).getText().trim();
				modelPush.content = et.getChild("content", ns).getText().trim();
				modelPush.imageUrl = et.getChild("imageUrl", ns).getText().trim();
				modelPush.linkUri = et.getChild("linkUri", ns).getText().trim();
				modelPush.turn = et.getChild("turn", ns).getText().trim();
				
				pushList.add(modelPush);

				System.out.println(et.getChild("serviceType", ns).getText().trim());
				System.out.println(et.getChild("msgType", ns).getText().trim());
				System.out.println(et.getChild("id", ns).getText().trim());
				System.out.println(et.getChild("title", ns).getText().trim());
				System.out.println(et.getChild("digest", ns).getText().trim());
				System.out.println(et.getChild("content", ns).getText().trim());
				System.out.println(et.getChild("imageUrl", ns).getText().trim());
				System.out.println(et.getChild("linkUri", ns).getText().trim());
				System.out.println(et.getChild("turn", ns).getText().trim());

				
			}
			
			return pushList;
			
		} catch (JDOMException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		return null;
	}
    
    public List xmlElements(String xmlDoc) {
        //创建一个新的字符串
        StringReader read = new StringReader(xmlDoc);
        //创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
        InputSource source = new InputSource(read);
        //创建一个新的SAXBuilder
        SAXBuilder sb = new SAXBuilder();
        try {
            //通过输入源构造一个Document
            Document doc = sb.build(source);
            //取的根元素
            Element root = doc.getRootElement();
            System.out.println(root.getName());//输出根元素的名称（测试）
            //得到根元素所有子元素的集合
            List jiedian = root.getChildren();
            //获得XML中的命名空间（XML中未定义可不写）
            Namespace ns = root.getNamespace();
            Element et = null;
            for(int i=0;i<jiedian.size();i++){
                et = (Element) jiedian.get(i);//循环依次得到子元素
               
                System.out.println(et.getChild("users_id",ns).getText());
                System.out.println(et.getChild("users_address",ns).getText());
            }
           
            et = (Element) jiedian.get(0);
            List zjiedian = et.getChildren();
            for(int j=0;j<zjiedian.size();j++){
                Element xet = (Element) zjiedian.get(j);
                System.out.println(xet.getName());
            }
        } catch (JDOMException e) {
            // TODO 自动生成 catch 块
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自动生成 catch 块
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args){
    	XmlDoc doc = new XmlDoc();
        String xml = "<?xml version=\"1.0\" encoding=\"gb2312\"?>"+
        "<Result xmlns=\"http://www.fiorano.com/fesb/activity/DBQueryOnInput2/Out/\">"+
           "<row resultcount=\"1\">"+
              "<users_id>1001     </users_id>"+
              "<users_name>wangwei   </users_name>"+
              "<users_group>80        </users_group>"+
              "<users_address>1001号   </users_address>"+
           "</row>"+
           "<row resultcount=\"1\">"+
              "<users_id>1002     </users_id>"+
              "<users_name>wangwei   </users_name>"+
              "<users_group>80        </users_group>"+
              "<users_address>1002号   </users_address>"+
           "</row>"+
        "</Result>";
        doc.xmlElements(xml);
    }
}
