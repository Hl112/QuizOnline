/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.listener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 *
 * @author HL
 */
public class InitSiteMap implements ServletContextListener {
    
    private final String SITEMAP_FILE = "siteMap.properties";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String path = context.getRealPath("/WEB-INF")+ "\\" + SITEMAP_FILE;
        FileReader f = null;
        BufferedReader br = null;
        try {
            f = new FileReader(path);
            br = new BufferedReader(f);
            Map<String, String> siteMap = new HashMap<>();
            while(br.ready()){
                String[] line = br.readLine().split("=");
                String key = line[0].trim();
                String vallue = line[1].trim();
                siteMap.put(key, vallue);
                
            }
            context.setAttribute("SITE", siteMap);
        } catch (IOException e) {
            System.out.println(" - Site Map Error");  
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Deployed");
    }
}
