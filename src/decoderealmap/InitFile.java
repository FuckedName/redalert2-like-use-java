package decoderealmap;

import main.MyLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class InitFileSection {
}

public class InitFile {
    Map<String, InitFileSection> map;
    List<String> list;
    Logger logger = MyLogger.logger;

    InitFile()
    {
        map = new HashMap<>();
        list = new ArrayList<>();
    }

    public Integer LoadFile(String filePath)
    {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null)
            {
                //System.out.println(line);
                list.add(line);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("read line count: " + list.size());

        return 1;

    }

    public StringBuilder getSectionData(String sectionName, String keyPerLine)
    {
        StringBuilder stringBuilder;
        stringBuilder = new StringBuilder();

        Integer currentSectionStartLine = findKey(sectionName);
        logger.info(currentSectionStartLine.toString());

        Integer nextSectionStart = findStartWith(currentSectionStartLine + 1, "[");
        logger.info(nextSectionStart.toString());

        Integer lastKeyInLine = findForeheadContain(nextSectionStart, keyPerLine);
        logger.info(lastKeyInLine.toString());

        //startline + 1: "[IsoMapPack5]"
        for (int i = currentSectionStartLine + 1; i <= lastKeyInLine; i++) {
            String line = list.get(i);
            //logger.info(line);
            int indexOf = line.indexOf("=");
            String substring = line.substring(indexOf + 1); // +1: =
            //logger.info(substring);
            stringBuilder.append(substring);
        }

        int length = stringBuilder.length();
        String substring = stringBuilder.substring(0, length);
        //logger.info(substring);

        return new StringBuilder(substring);
    }

    public Integer findKey(String keyword)
    {
        logger.info("input keyword: " + keyword);
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);

            //System.out.println(i + ": " + s);
            if (s.equals(keyword))
                return i;
        }
        return -1;
    }


    public Integer findStartWith(Integer startLine, String keyword)
    {
        logger.info("input keyword: " + keyword);
        for (int i = startLine; i < list.size(); i++) {
            String s = list.get(i);

            //logger.info(i + ": " + s);
            if (s.startsWith(keyword))
                return i;
        }
        return -1;
    }

    public Integer findForeheadContain(Integer endLine, String keyword)
    {
        logger.info("input keyword: " + keyword);
        for (int i = endLine; i > 0; i--)
        {
            String s = list.get(i);

            //logger.info(i + ": " + s);
            if (s.contains(keyword))
                return i;
        }
        return -1;
    }
}
