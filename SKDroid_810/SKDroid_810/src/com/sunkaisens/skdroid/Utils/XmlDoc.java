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
		// ����һ���µ��ַ���
		StringReader read = new StringReader(xmlDoc);
		// �����µ�����ԴSAX ��������ʹ�� InputSource ������ȷ����ζ�ȡ XML ����
		InputSource source = new InputSource(read);
		// ����һ���µ�SAXBuilder
		SAXBuilder sb = new SAXBuilder();
		try {
			// ͨ������Դ����һ��Document
			Document doc = sb.build(source);
			// ȡ�ĸ�Ԫ��
			Element root = doc.getRootElement();
			System.out.println(root.getName()); // �����Ԫ�ص����ƣ����ԣ�
			// �õ���Ԫ��������Ԫ�صļ���
			List jiedian = root.getChildren();
			// ���XML�е������ռ䣨XML��δ����ɲ�д��
			Namespace ns = root.getNamespace();
			Element et = null;
			for (int i = 0; i < jiedian.size(); i++) {
				et = (Element) jiedian.get(i); // ѭ�����εõ���Ԫ��
				
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
			// TODO �Զ����� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����� catch ��
			e.printStackTrace();
		}
		return null;
	}
    
    public List xmlElements(String xmlDoc) {
        //����һ���µ��ַ���
        StringReader read = new StringReader(xmlDoc);
        //�����µ�����ԴSAX ��������ʹ�� InputSource ������ȷ����ζ�ȡ XML ����
        InputSource source = new InputSource(read);
        //����һ���µ�SAXBuilder
        SAXBuilder sb = new SAXBuilder();
        try {
            //ͨ������Դ����һ��Document
            Document doc = sb.build(source);
            //ȡ�ĸ�Ԫ��
            Element root = doc.getRootElement();
            System.out.println(root.getName());//�����Ԫ�ص����ƣ����ԣ�
            //�õ���Ԫ��������Ԫ�صļ���
            List jiedian = root.getChildren();
            //���XML�е������ռ䣨XML��δ����ɲ�д��
            Namespace ns = root.getNamespace();
            Element et = null;
            for(int i=0;i<jiedian.size();i++){
                et = (Element) jiedian.get(i);//ѭ�����εõ���Ԫ��
               
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
            // TODO �Զ����� catch ��
            e.printStackTrace();
        } catch (IOException e) {
            // TODO �Զ����� catch ��
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
              "<users_address>1001��   </users_address>"+
           "</row>"+
           "<row resultcount=\"1\">"+
              "<users_id>1002     </users_id>"+
              "<users_name>wangwei   </users_name>"+
              "<users_group>80        </users_group>"+
              "<users_address>1002��   </users_address>"+
           "</row>"+
        "</Result>";
        doc.xmlElements(xml);
    }
}
