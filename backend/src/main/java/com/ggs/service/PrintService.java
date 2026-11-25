package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.DepartmentMapper;
import com.ggs.mapper.ScoreMapper;
import com.ggs.mapper.SemesterMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Department;
import com.ggs.pojo.Score;
import com.ggs.pojo.Semester;
import com.ggs.pojo.Student;
import com.ggs.util.DataScopeUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 证明打印（批次N）：成绩单 / 在读证明，OpenPDF 生成，内置中文字体 STSong-Light。
 */
@Service
public class PrintService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private SemesterMapper semesterMapper;

    private BaseFont baseFont() throws Exception {
        return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
    }

    /** 权限：学生本人或管理员/教授（数据范围） */
    private Student requireStudent(Integer sid) {
        if (sid == null) {
            throw new BusinessException("缺少学生学号");
        }
        Student student = studentMapper.getById(sid);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        Clazz clazz = clazzMapper.getById(student.getCid());
        DataScopeUtil.requireInScope(clazz == null ? null : clazz.getDid());
        return student;
    }

    /** 成绩单 PDF */
    public void transcript(Integer sid, HttpServletResponse response) throws Exception {
        Student student = requireStudent(sid);
        Clazz clazz = clazzMapper.getById(student.getCid());
        Department dept = clazz == null ? null : departmentMapper.getById(clazz.getDid());
        List<Score> scores = scoreMapper.listByStudentAndSemester(sid, null);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename*=utf-8''transcript_" + sid + ".pdf");
        OutputStream out = response.getOutputStream();

        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(doc, out);
        doc.open();
        BaseFont bf = baseFont();
        Font titleFont = new Font(bf, 18, Font.BOLD);
        Font headFont = new Font(bf, 11, Font.BOLD);
        Font bodyFont = new Font(bf, 10, Font.NORMAL);

        Paragraph title = new Paragraph("高 校 教 务 管 理 系 统", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        Paragraph sub = new Paragraph("成 绩 单", new Font(bf, 14, Font.BOLD));
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(new Paragraph(" ", bodyFont));

        String info = "学号：" + student.getSid() + "    姓名：" + nvl(student.getName())
                + "    院系：" + (dept == null ? "" : nvl(dept.getName()))
                + "    班级：" + (clazz == null ? "" : nvl(clazz.getName()))
                + "    打印日期：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        doc.add(new Paragraph(info, bodyFont));
        doc.add(new Paragraph(" ", bodyFont));

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        String[] heads = {"课程", "学分", "平时", "期中", "期末", "总评", "绩点"};
        for (String h : heads) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        double totalCredit = 0;
        double gpaSum = 0;
        double creditForGpa = 0;
        for (Score s : scores) {
            table.addCell(cell(nvl(s.getCourseName()), bodyFont));
            table.addCell(cell(s.getCredit() == null ? "-" : fmt(s.getCredit()), bodyFont));
            table.addCell(cell(s.getUsual() == null ? "-" : fmt(s.getUsual()), bodyFont));
            table.addCell(cell(s.getMid() == null ? "-" : fmt(s.getMid()), bodyFont));
            table.addCell(cell(s.getFinalScore() == null ? "-" : fmt(s.getFinalScore()), bodyFont));
            table.addCell(cell(s.getTotal() == null ? "-" : fmt(s.getTotal()), bodyFont));
            table.addCell(cell(s.getGp() == null ? "-" : fmt(s.getGp()), bodyFont));
            double credit = s.getCredit() == null ? 0 : s.getCredit();
            totalCredit += credit;
            if (s.getGp() != null) {
                gpaSum += s.getGp() * credit;
                creditForGpa += credit;
            }
        }
        doc.add(table);

        doc.add(new Paragraph(" ", bodyFont));
        double gpa = creditForGpa > 0 ? Math.round(gpaSum / creditForGpa * 100.0) / 100.0 : 0.0;
        doc.add(new Paragraph("总学分：" + Math.round(totalCredit * 10.0) / 10.0 + "    平均学分绩点（GPA）：" + gpa, headFont));
        doc.add(new Paragraph(" ", bodyFont));
        Paragraph stamp = new Paragraph("教务处（成绩专用章）", bodyFont);
        stamp.setAlignment(Element.ALIGN_RIGHT);
        doc.add(stamp);
        doc.close();
    }

    /** 在读证明 PDF */
    public void enrollment(Integer sid, HttpServletResponse response) throws Exception {
        Student student = requireStudent(sid);
        Clazz clazz = clazzMapper.getById(student.getCid());
        Department dept = clazz == null ? null : departmentMapper.getById(clazz.getDid());
        Semester current = semesterMapper.getCurrent();

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename*=utf-8''enrollment_" + sid + ".pdf");
        OutputStream out = response.getOutputStream();

        Document doc = new Document(PageSize.A4, 60, 60, 80, 60);
        PdfWriter.getInstance(doc, out);
        doc.open();
        BaseFont bf = baseFont();
        Font titleFont = new Font(bf, 18, Font.BOLD);
        Font bodyFont = new Font(bf, 12, Font.NORMAL);
        Font boldFont = new Font(bf, 12, Font.BOLD);

        Paragraph title = new Paragraph("在 读 证 明", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph(" ", bodyFont));
        doc.add(new Paragraph(" ", bodyFont));

        Paragraph line1 = new Paragraph("兹证明 " + nvl(student.getName()) + " 同学，学号 " + student.getSid() + "，", bodyFont);
        line1.setFirstLineIndent(24);
        doc.add(line1);
        Paragraph line2 = new Paragraph("系我校" + (dept == null ? "" : nvl(dept.getName()))
                + (clazz == null ? "" : nvl(clazz.getName())) + "学生，学制四年。", bodyFont);
        line2.setFirstLineIndent(24);
        doc.add(line2);
        Paragraph line3 = new Paragraph("该生目前在校就读"
                + (current == null ? "" : "（" + nvl(current.getName()) + "）")
                + "，情况属实，特此证明。", bodyFont);
        line3.setFirstLineIndent(24);
        doc.add(line3);

        doc.add(new Paragraph(" ", bodyFont));
        doc.add(new Paragraph(" ", bodyFont));
        Paragraph date = new Paragraph("打印日期：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), bodyFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        doc.add(date);
        Paragraph stamp = new Paragraph("教务处", boldFont);
        stamp.setAlignment(Element.ALIGN_RIGHT);
        doc.add(stamp);
        doc.close();
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String fmt(Double d) {
        return String.valueOf(Math.round(d * 10.0) / 10.0);
    }
}
