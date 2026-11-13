package com.ggs.service;

import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Student;
import com.ggs.util.DataScopeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 数据范围校验：学生所属班级院系必须在本人范围内 */
    private void checkScopeByCid(Integer cid) {
        if (cid == null) {
            return;
        }
        Clazz clazz = clazzMapper.getById(cid);
        DataScopeUtil.requireInScope(clazz == null ? null : clazz.getDid());
    }

    @Override
    public List<Student> listStu() {
        return studentMapper.listStu();
    }

    @Override
    public int delete(Long sid) {
        Student exist = studentMapper.getById(sid.intValue());
        if (exist != null) {
            checkScopeByCid(exist.getCid());
        }
        return studentMapper.delete(sid);
    }

    @Override
    public List<Student> getStuByAge(int age) {
        return studentMapper.getStuByAge(age);
    }

    @Override
    public int updateAge(long sid, long age) {
        return studentMapper.updateAge(sid, (int) age);
    }

    @Override
    public int addStu(Student student) {
        checkScopeByCid(student.getCid());
        return studentMapper.addStu(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddStudents(List<Student> students) {
        int count = 0;
        for (Student student : students) {
            studentMapper.addStu(student);
            count++;
        }
        return count;
    }

    @Override
    public int updateStu(Student student) {
        checkScopeByCid(student.getCid());
        return studentMapper.updateStu(student);
    }

    @Override
    public List<Student> getStu(Student student) {
        return studentMapper.getStu(student);
    }

    @Override
    public int countStu(String name, String cname, Integer did) {
        return studentMapper.countStu(name, cname, did);
    }

    @Override
    public Student getById(int sid) {
        return studentMapper.getById(sid);
    }

    @Override
    public Student login(String account, String password) {
        // 学生登录：account 可为学号(sid)或库中 username
        Student stu = null;
        try {
            stu = studentMapper.getById(Integer.parseInt(account));
        } catch (NumberFormatException e) {
            stu = studentMapper.getByUsername(account);
        }
        if (stu == null) {
            return null;
        }
        if (stu.getStatus() == null || stu.getStatus() != 1) {
            return null;
        }
        String stored = stu.getPassword();
        if (stored == null || stored.isEmpty()) {
            return null;
        }
        // 兼容明文密码并在首次明文登录成功后即时升级为 BCrypt
        if (!stored.startsWith("$2a$") && !stored.startsWith("$2b$") && stored.equals(password)) {
            String encoded = passwordEncoder.encode(password);
            studentMapper.updatePassword(stu.getSid(), encoded);
            stu.setPassword(encoded);
            return stu;
        }
        if (passwordEncoder.matches(password, stored)) {
            return stu;
        }
        return null;
    }

    @Override
    public int resetPassword(int sid) {
        return updatePassword(sid, "123456");
    }

    @Override
    public int updatePassword(int sid, String password) {
        return studentMapper.updatePassword(sid, passwordEncoder.encode(password));
    }
}