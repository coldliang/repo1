package com.cqupt.net;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class WebService {

    public static final int CONNECTION_TIMEOUT = -1;
    public static final int SUCCESS = 1;

	public SoapObject CallWebService(String MethodName, Map<String, String> Params) {
		 SoapObject result = null;
        // 1��ָ��webservice�������ռ�͵��õķ�����
        SoapObject request = new SoapObject("http://tempuri.org/", MethodName);
        // 2�����õ��÷����Ĳ���ֵ�����û�в���������ʡ�ԣ�
        if (Params != null) {
            Iterator<Entry<String, String>> iter = Params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String,String>) iter.next();
                request.addProperty(entry.getKey(),entry.getValue());
            }
        }
        // 3�����ɵ���Webservice������SOAP������Ϣ������Ϣ��SoapSerializationEnvelope��������
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.bodyOut = request;
        // c#д��Ӧ�ó������������
        envelope.dotNet = true;     
        //genymotionģ����
        //HttpTransportSE ht = new HttpTransportSE("http://10.0.3.2:60905/webservice/WebService.asmx");
        //ģ����
        //HttpTransportSE ht = new HttpTransportSE("http://10.0.2.2:60905/webservice/WebService.asmx",10000);
        //������1��ַ
        //HttpTransportSE ht = new HttpTransportSE("http://202.202.43.244/webservice/WebService.asmx",3000);
        //������2��ַ
        //HttpTransportSE ht = new HttpTransportSE("http://202.202.43.44/webservice/WebService.asmx",3000);
        HttpTransportSE ht = new HttpTransportSE("http://202.202.43.44:80/WebService.asmx",3000);
        //����IIS��ַ
        //HttpTransportSE ht = new HttpTransportSE("http://172.23.7.189:3000/WebService.asmx",3000);
        
        // ʹ��call��������WebService����
        try {
            ht.call("http://tempuri.org/"+MethodName, envelope);
            //String s = envelope.getResponse().toString();
            if(envelope.bodyIn instanceof SoapFault){
            	String str= ((SoapFault) envelope.bodyIn).faultstring;
            	Log.i("", str);
            }else{
            	result= (SoapObject) envelope.bodyIn;
            }
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return result;
    }

}
