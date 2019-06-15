package noodlebrain.bpm;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.rauschig.jarchivelib.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Functions pertaining to interfacing with the cardiovascular (cdvs).
public class CdvsUtils
{
    private static String cdvsURL = "https://cdvs.blazingk.in/";

    static List<PulseEntry> getPulse(String pkg) throws IOException
    {
        Gson gson = new Gson();
        URL pulseURL = new URL(cdvsURL + "/cdg/" + pkg + "/pls.json");

        String json = IOUtils.toString(pulseURL, "UTF-8");
        Type listType = new TypeToken<List<PulseEntry>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    // Given a URL, downloads and unzips a package, putting it into the Packages directory
    static void downloadPackage(String url, String name) throws IOException
    {
        URL downloadURL = new URL(url);

        // download tarball for package
        File tarball = new File("Packages/" + name + ".tar.gz");
        System.out.println("Downloading package " + tarball.getName() + "...");
        download(downloadURL, tarball);
        System.out.println("Done.");

        // unzip tarball and put it in the directory
        File tar = new File("Packages/" + name + ".tar");
        File dest = new File("Packages/" + name);

        // delete tarball and tar files on exit
        tarball.deleteOnExit();
        tar.deleteOnExit();

        // clean directory if it already contains files
        if (dest.exists())
        {
            FileUtils.cleanDirectory(dest);
        }

        System.out.println("Extracting package " + name + "...");

        // first step of unzipping: decompress GZIP
        Compressor compressor = CompressorFactory.createCompressor(tarball);
        compressor.decompress(tarball, tar);

        Archiver archiver = ArchiverFactory.createArchiver("tar");
        archiver.extract(tar, dest);
        System.out.println("Done.");
    }

    private static void download(URL downloadURL, File tarball) throws IOException
    {
        // create HTTP client; we need to support redirects
        CloseableHttpClient httpClient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();

        HttpGet httpGet = new HttpGet(downloadURL.toString());
        HttpEntity httpEntity = null;

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet))
        {
            httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();
            FileUtils.copyInputStreamToFile(inputStream, tarball);
        }
        finally
        {
            EntityUtils.consumeQuietly(httpEntity);
        }
    }


    // NOT FUNCTIONAL due to Rails's Cross-Site Request Forgery protection
    // prompt for a username and password; return a session cookie from cdvs
    static String login() throws IOException
    {
        String email;
        String password;
        String sessionCookie = " ";

        // prompt username and password from stdin
        Scanner sc = new Scanner(System.in);
        System.out.println("Please login with your cdvs email and password.");
        System.out.println("Email: ");
        email = sc.nextLine();
        System.out.println("Password: ");
        password = sc.nextLine();

        sc.close();

        // send HTTP POST login request and receive a cookie
        CloseableHttpClient httpClient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
        HttpPost httpPost = new HttpPost("https://cdvs.blazingk.in/login");

        // set params for login HTTP POST
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("session[email]", email));
        params.add(new BasicNameValuePair("session[password]", password));
        params.add(new BasicNameValuePair("commit", "Log+in"));

        httpPost.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        System.out.println(httpResponse.getStatusLine().getStatusCode());

        Header[] headers = httpResponse.getHeaders("Set-Cookie");
        for (Header header : headers)
        {
            if (header.getName().equals("_cardiovascular_session"))
            {
                sessionCookie = header.getValue();
            }
        }

        return sessionCookie;
    }

    // creates a tarball from the current directory
    static void createTarball() throws IOException
    {
        File currDir = new File("");
        File absDir = currDir.getAbsoluteFile();
        String dirName = absDir.getName();


        File tarDir = new File("tarball");
        File source = new File("./Source");
        File heartbeat = new File("./heartbeat.yaml");


        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");

        System.out.println("Creating tarball for package " + dirName + "...");
        File archive = archiver.create(dirName, tarDir, source, heartbeat);
        System.out.println("Done.");

        // move tarball to project directory and delete old files
        FileUtils.copyFile(archive, new File(archive.getName()));

        archive.delete();
        tarDir.delete();
    }

}
