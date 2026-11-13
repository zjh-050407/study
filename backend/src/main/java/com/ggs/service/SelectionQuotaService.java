package com.ggs.service;

public interface SelectionQuotaService {

    /**
     * 占用一个名额。成功返回 true；名额已满/重复选择返回 false。
     * 失败原因通过 result 参数带出（"名额已满" / "已选"）。
     */
    QuotaResult tryOccupy(int sid, int courseId, Integer semesterId, int capacity);

    /** 释放一个名额（退选/选课落库失败回滚） */
    void release(int sid, int courseId, Integer semesterId);

    class QuotaResult {
        public final boolean ok;
        public final String message;

        public QuotaResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        public static QuotaResult success() {
            return new QuotaResult(true, null);
        }

        public static QuotaResult fail(String message) {
            return new QuotaResult(false, message);
        }
    }
}
