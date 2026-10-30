package com.ggs.service;

import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.NoticeMapper;
import com.ggs.pojo.Notice;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public ResultVO.PageResult<Notice> listNotice(int page, int size) {
        int offset = (page - 1) * size;
        List<Notice> list = noticeMapper.listByPage(offset, size);
        int total = noticeMapper.countAll();
        return new ResultVO.PageResult<>(list, total, page, size);
    }

    @Override
    public List<Notice> latest(int limit) {
        return noticeMapper.listLatest(limit);
    }

    @Override
    public void add(Notice notice) {
        if (notice == null || !StringUtils.hasText(notice.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        if (!StringUtils.hasText(notice.getContent())) {
            throw new BusinessException("公告内容不能为空");
        }
        notice.setType(StringUtils.hasText(notice.getType()) ? notice.getType() : "system");
        notice.setTargetRole(StringUtils.hasText(notice.getTargetRole()) ? notice.getTargetRole() : "all");
        notice.setStatus(1);
        notice.setPublisher(JwtInterceptor.USERNAME_HOLDER.get());
        LocalDateTime now = LocalDateTime.now();
        notice.setPublishTime(now);
        notice.setCreateTime(now);
        noticeMapper.insert(notice);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || noticeMapper.delete(id) == 0) {
            throw new BusinessException("删除失败，公告不存在");
        }
    }
}