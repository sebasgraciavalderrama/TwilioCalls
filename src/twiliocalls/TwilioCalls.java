/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twiliocalls;

import java.net.URI;
import java.net.URISyntaxException;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.joda.time.DateTimeZone;

/**
 *
 * @author kcastrop
 */
public class TwilioCalls {

    /**
     * @param args the command line arguments
     *
     */
    
    public static final String ACCOUNT_SID = "ACc8fdd96ff9507fe3e4c0e9add4fab253";
    public static final String AUTH_TOKEN = "c8e8189b28632bf5630573b0cc1b0ea1";
    
    
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        // Find your Account Sid and Auth Token at twilio.com/console
        ArrayList<String> numbers = new ArrayList<>();
        
        numbers.add("+18032448418");
         
        //Test alert --------------------------------------------------------------
        Random rand = new Random();
        int  n = rand.nextInt(99) + 1;
        String alerta = "Cliente Coomeva. Alerta, espacio en " + Integer.toString(n) + " porciento";
        //-------------------------------------------------------------------------
         
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        
        call(numbers, alerta);
    }
    
    public static void call(ArrayList<String> numbers, String alerta) throws Exception{
        
        ArrayList<String> call_ids = new ArrayList<>(); 
        
        String answer_url = buildCall(alerta);
        
        //Wait for the xml to upload
        Thread.sleep(1*1000);
        
        for(String number: numbers){
            call_ids.add(makeCall(number, answer_url));
        }
        
        
        System.out.println("Esperando n segundos para obtener informacion de la(s) llamadas(s)");
        Thread.sleep(50*1000);
        
        getCallDetails(call_ids);
        
    }
    
    public static String buildCall(String alerta) throws Exception{
        //XML file creation -------------------------------------------------------
        Element root=new Element("Response");
        Document doc=new Document();

        Element child=new Element("Say");
        child.setAttribute("voice", "man");
        child.setAttribute("language", "es");
        child.setAttribute("loop", "3");
        child.addContent(alerta);

        root.addContent(child);

        doc.setRootElement(root);

        XMLOutputter outter=new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        outter.output(doc, new FileWriter(new File("voice.xml")));
        
        String server = "www.sebasgracia.com";
        int port = 21;
        String user = "ibm@sebasgracia.com";
        String pass = "Welcome1#";
        
 
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
 
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            File firstLocalFile = new File("voice.xml");
 
            String firstRemoteFile = "voice.xml";
            InputStream inputStream = new FileInputStream(firstLocalFile);
 
            System.out.println("Subiendo archivo al servidor FTP");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.println("Archivo subido con exito!");
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        String answer_url = "http://www.sebasgracia.com/ibm/voice.xml";
        System.out.println("Link xml: " + answer_url);
        System.out.println("");
                
        return answer_url;
    
    }
    
    public static String makeCall(String number, String answer_url) throws Exception{
        
        System.out.println("Llamando al numero: " + number);
        Call call = Call.creator(new PhoneNumber(number), new PhoneNumber("+573157959868"),
                new URI(answer_url)).create();

        String call_id = call.getSid();
        
        return call_id;
    }
    
    
    
    public static void getCallDetails(ArrayList<String> calls){
        System.out.println("");
        System.out.println("--------------------- Detalles de la(s) llamada(s) ---------------------");
        
        for(String c: calls){
            Call call = Call.fetcher(c).fetch();
            System.out.println("Numero llamado: " + call.getTo());
            System.out.println("Duracion de la llamada: " + call.getDuration());
            System.out.println("Costo de la llamada: " + call.getPrice());
            System.out.println("Hora de la llamada: " + call.getEndTime().withZone(DateTimeZone.forID("America/Bogota")));
            System.out.println("---------------------------------------------------------");
        }    
    }
    
     
}
