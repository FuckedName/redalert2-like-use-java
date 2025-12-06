package main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;

class ConfigItem
{
    long id;
    String name;
    int width;
    int height;
    int price;
    String imagePath;

    public ConfigItem(long id, String name, int width, int height, int price, String imagePath)
    {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.price = price;
        this.imagePath = imagePath;
    }
}

public class Config {
   static Logger logger = MyLogger.logger;
    public static int thisDeviceType;
    public static HashMap<Integer, ConfigItem> parameterHashMap;

    Config(String file)
    {
        parameterHashMap = new HashMap<>();

        try {
            FileInputStream fin = new FileInputStream(Global.srcPath + file);
            InputStreamReader reader = new InputStreamReader(fin, StandardCharsets.UTF_8);
            BufferedReader buffReader = new BufferedReader(reader);

            String s;
            while ((s = buffReader.readLine()) != null) {
                logger.info(s);
                parse(s);
            }

            buffReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void parse(String key)
    {
        String[] splits = key.split("\t");
        if (splits[0].equals("ThisDeviceType"))
        {
            thisDeviceType = Integer.parseInt(splits[1]);
            logger.info("thisDeviceType: " + thisDeviceType);
        }
        else if (key.startsWith("Building"))
        {
            logger.info("Building: "+key);
            saveParameter(key);
        }
        else if (key.startsWith("SeaUnit"))
        {
            logger.info("SeaUnit: "+key);
            saveParameter(key);
        }
        else if (key.startsWith("LandUnit"))
        {
            logger.info("LandUnit: "+key);
            saveParameter(key);
        }
        else if (key.startsWith("AirUnit"))
        {
            logger.info("AirUnit: "+key);
            saveParameter(key);
        }
    }

    void saveParameter(String value)
    {
        String[] split = value.split("\t");
        for (int i = 0; i < split.length; i++)
        {
            logger.info(split[i]);
        }

        int id1 = Integer.parseInt(split[1]);
        int sub_id = Integer.parseInt(split[2]);
        String name = split[3];
        int width = Integer.parseInt(split[4]);
        int height = Integer.parseInt(split[5]);
        int price = Integer.parseInt(split[6]);
        String image_path1 = split[7];
        String image_path2 = split[8];
        logger.info("AirUnit: " + value);
        int id = id1 * 10000 + sub_id;
        String path = image_path1+"\\"+image_path2+"\\";
        parameterHashMap.put(id,
                new ConfigItem(id, name, width, height, price, path));
    }
}
