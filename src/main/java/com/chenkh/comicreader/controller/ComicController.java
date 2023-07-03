package com.chenkh.comicreader.controller;

import com.chenkh.comicreader.entity.Book;
import com.chenkh.comicreader.entity.Variables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

///comic/booklist.html
@Controller
@RequestMapping("/comic")
public class ComicController {
    
    final
    ObjectMapper objectMapper;
    List<Book> books;

    public String jsonPath;
    public String imgPath;

    public ComicController(ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        this.jsonPath = System.getProperty("jsonPath");
        this.imgPath = System.getProperty("imgPath");
        books = objectMapper.readValue(new File(jsonPath), new TypeReference<>() {});
    }


    @GetMapping("/booklist.html")
    public void booklist(HttpServletResponse response) throws IOException {
        books = objectMapper.readValue(new File(jsonPath), new TypeReference<>() {});
        StringBuilder sb = new StringBuilder();
        for (Book book : books) {
            String maxchapter = "";
            File file = new File(imgPath, book.getName());
            if (file.exists() && file.isDirectory()) {
                File[] chapters = file.listFiles();
                if (chapters != null) {
                    Optional<File> max = Arrays.stream(chapters).max(Comparator.comparingInt(tempfile -> Integer.parseInt(tempfile.getName().substring(0, 3))));
                    if (max.isPresent()) {
                        maxchapter = max.get().getName().substring(0,3);
                    }
                }
            }

            String template_book_link = Variables.TEMPLATE_BOOK_LINK
                    .replace("{url}","chapterlist.html?bookname=" + encodeURLSafe(book.getName()))
                    .replace("{status}",book.isEnd() ? "已完结" : "连载中")
                    .replace("{set_status}","switchend?bookid="+book.getId())
                    .replace("{name}",book.getName())
                    .replace("{readAt}",book.getReadAt())
                    .replace("{total}",maxchapter)
                    .replace("{moveup}","move?type=up&bookid="+book.getId())
                    .replace("{movedown}","move?type=down&bookid="+book.getId());

            if (!"000".equals(book.getReadAt()) && !"001".equals(book.getReadAt()) && !maxchapter.equals(book.getReadAt())) {
                template_book_link = template_book_link.replace("#e6e6e6", "#faf572");
            }

            sb.append(template_book_link);

        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(Variables.TEMPLATE_BOOKS.replace("        <li_here />", sb.toString()));
        out.flush();
        out.close();
    }

    @GetMapping("/switchend")
    public void switchEnd(HttpServletResponse response, String bookid) throws IOException {
        for (Book book : books) {
            if (bookid.equals(book.getId())) {
                book.setEnd(!book.isEnd());
                saveJson();
                break;
            }
        }
        response.sendRedirect("/comic/booklist.html");
    }

    @GetMapping("/move")
    public void move(HttpServletResponse response, String type, String bookid) throws IOException {
        int index = 0;
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (bookid.equals(book.getId())) {
                index = i;
            }
        }

        if (index == 0 && "up".equals(type)) {
            response.sendRedirect("/comic/booklist.html");
        }else if (index == books.size()-1 && "down".equals(type)) {
            response.sendRedirect("/comic/booklist.html");
        }else {
            switch (type) {
                case "up" -> Collections.swap(books, index, index - 1);
                case "down" -> Collections.swap(books, index, index + 1);
                default -> {
                }
            }
            saveJson();
            response.sendRedirect("/comic/booklist.html");
        }


    }


    @GetMapping("/chapterlist.html")
    public void chapterlist(HttpServletResponse response, String bookname) throws IOException {
        bookname = decodeURLSafe(bookname);
        
        File file = new File(imgPath, bookname);
        if (!file.exists() || !file.isDirectory()) {
            response.sendError(404);
            return;
        }
        
        File[] chapters = file.listFiles();
        StringBuilder sb = new StringBuilder();
        if (chapters == null) {
            sb.append("            <li>无章节</li> \n");
        }else {
            List<File> collect = Arrays.stream(chapters).sorted(Comparator.comparingInt(tempfile -> Integer.parseInt(tempfile.getName().substring(0, 3)))).toList();
            for (File chapterDir : collect) {
                String name = chapterDir.getName();
//                sb.append("            <div><a href=\"imglist.html?bookname=")
//                        .append(encodeURLSafe(bookname))
//                        .append("&chapter=").append(encodeURLSafe(name))
//                        .append("\">").append(name).append("</a></div> \n");

                sb.append(Variables.TEMPLATE_CHAPTER_LINK
                        .replace("{url}", "imglist.html?bookname=" + encodeURLSafe(bookname) + "&chapter=" + encodeURLSafe(name))
                        .replace("{chapter_name}",name)
                );
            }
        }
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(Variables.TEMPLATE_BOOK_CHAPTERS.replace("        <li_here />", sb.toString()).replace("{title}",bookname));
        out.flush();
        out.close();

    }

    @GetMapping("/imglist.html")
    public void imglist(HttpServletResponse response, String bookname, String chapter) throws IOException {
        bookname = decodeURLSafe(bookname);
        chapter = decodeURLSafe(chapter);

        String number = chapter.substring(0,3);
        for (Book book : books) {
            if (bookname.equals(book.getName())) {
                book.setReadAt(number);
            }
        }
        saveJson();
        
        String imgPath = System.getProperty("imgPath");
        File file = new File(imgPath+File.separator+bookname, chapter);
        if (!file.exists() || !file.isDirectory()) {
            response.sendError(404);
            return;
        }

        String last = "";
        String next = "";
        File chaptersfile = new File(imgPath, bookname);
        File[] chapters = chaptersfile.listFiles();
        if (chapters != null) {
            List<String> collect = Arrays.stream(chapters).map(File::getName).sorted(Comparator.comparingInt(chaptername -> Integer.parseInt(chaptername.substring(0, 3)))).toList();
            int i = collect.indexOf(chapter);
            if (i != -1) {
                if (i == 0) {
                    next = collect.get(1);
                }else if (i == collect.size()-1) {
                    last = collect.get(i-1);
                }else {
                    last = collect.get(i-1);
                    next = collect.get(i+1);
                }
            }
        }

        File[] imgs = file.listFiles();
        StringBuilder sb = new StringBuilder();
        if (imgs == null) {
            sb.append("            <li>无图片</li> \n");
        }else {
            List<File> collect = Arrays.stream(imgs).sorted(Comparator.comparingInt(tempfile -> Integer.parseInt(tempfile.getName().substring(0, 3)))).toList();

            for (File img : collect) {
//                sb.append("            <img src=\"getImage?path=")
//                        .append(encodeURLSafe(img.getAbsolutePath()))
//                        .append("\" /> \n");
                sb.append("            <img src=\"/hanman/images/")
                        .append(replaceURLSafe(bookname)).append("/").append(replaceURLSafe(chapter)).append("/").append(img.getName())
                        .append("\" /> \n");
            }
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(Variables.TEMPLATE_CHAPTER_IMGS
                .replace("            <img_here />", sb.toString())
                .replace("{chapter}",chapter)
                .replace("{chapter_list}","chapterlist.html?bookname=" + encodeURLSafe(bookname))
                .replace("{last}", "".equals(last) ? "" : "imglist.html?bookname=" + encodeURLSafe(bookname) + "&chapter=" + encodeURLSafe(last))
                .replace("{next}", "".equals(next) ? "" : "imglist.html?bookname=" + encodeURLSafe(bookname) + "&chapter=" + encodeURLSafe(next))
        );
        out.flush();
        out.close();
        
    }
    
    public void saveJson() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(books);
        json = json.replaceAll("},\\{","}, \n    {").replace("[{","[\n    {").replace("}]","} \n]");
        try (FileOutputStream fos = new FileOutputStream(jsonPath)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @GetMapping(value = "/getImage")
//    public void getImage(HttpServletResponse response,String path) throws IOException {
//        path = decodeURLSafe(path);
//
//        try (ServletOutputStream outputStream = response.getOutputStream();
//             FileInputStream fileInputStream = new FileInputStream(path);) {
//            
//            BufferedImage image = ImageIO.read(fileInputStream);
//            response.setContentType("image/png");
//            if (image != null) {
//                ImageIO.write(image, "png", outputStream);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static String replaceURLSafe(String str) {
        return str.replaceAll("&", "&amp;").replaceAll("\\?", "%3F");
    }

    public static String encodeURLSafe(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8))
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "");
    }

    
    public static String decodeURLSafe(String content) {
        byte[] decode = Base64.getDecoder().decode(
                content.replace("-", "+").replace("_", "/")
                        .getBytes(StandardCharsets.UTF_8));
        return new String(decode, StandardCharsets.UTF_8);
    }
}
