import { createRouter, createWebHashHistory } from "vue-router";
import Login from "@/components/Login.vue";
import Home from "@/components/Home.vue";
import StuHome from "@/components/StuHome.vue";
import StuPlaceholder from "@/components/StuPlaceholder.vue";
import StudentManager from "@/components/StudentManager.vue";
import SemesterManager from "@/components/SemesterManager.vue";
import T1 from "@/components/T1.vue";
import T2 from "@/components/T2.vue";
import T3 from "@/components/T3.vue";
import T4 from "@/components/T4.vue";
import ClazzManager from "@/components/ClazzManager.vue";
import CourseManager from "@/components/CourseManager.vue";
import ScoreManager from "@/components/ScoreManager.vue";
import CourseArrangeManager from "@/components/CourseArrangeManager.vue";
import SystemInit from "@/components/SystemInit.vue";
import RegisterApproval from "@/components/RegisterApproval.vue";
import OperationLog from "@/components/OperationLog.vue";
import ScoreWarning from "@/components/ScoreWarning.vue";
import NoticeManager from "@/components/NoticeManager.vue";
import CourseSelectionManager from "@/components/CourseSelectionManager.vue";
import { ElMessage } from 'element-plus'

const routeRoleMap = {
  '/home/department': ['professor', 'admin'],
  '/home/t2': ['professor', 'admin'],
  '/home/semester': ['professor', 'admin'],
  '/home/systemInit': ['admin'],
  '/home/registerApproval': ['admin'],
  '/home/operationLog': ['admin'],
}

const routes = [
    {
        path: "/",
        redirect: "/home"
    },
    {
        path: "/home",
        component: Home,
        redirect: "/home/t1",
        children: [
            { path: "t1", component: T1 },
            { path: "studentManager", component: StudentManager },
            { path: "t2", component: T2 },
            { path: "t3", component: T3 },
            { path: "t4", component: T4 },
            { path: "department", component: T4 },
            { path: "clazz", component: ClazzManager },
            { path: "semester", component: SemesterManager },
            { path: "course", component: CourseManager },
            { path: "courseArrange", component: CourseArrangeManager },
            { path: "score", component: ScoreManager },
            { path: "scoreWarning", component: ScoreWarning },
            { path: "courseSelection", component: CourseSelectionManager },
            { path: "notice", component: NoticeManager },
            { path: "systemInit", component: SystemInit },
            { path: "registerApproval", component: RegisterApproval },
            { path: "operationLog", component: OperationLog },
        ]
    },
    {
        path: "/stu",
        component: StuHome,
        redirect: "/stu/notice",
        children: [
            { path: "notice", component: NoticeManager },
            { path: "schedule", component: StuPlaceholder, props: { title: '我的课表', desc: '这里将展示你的个人课程表，含上课时间、地点与任课教师。模块建设中，敬请期待。' } },
            { path: "score", component: StuPlaceholder, props: { title: '我的成绩', desc: '这里将展示你的各科成绩与绩点。模块建设中，敬请期待。' } },
            { path: "selection", component: StuPlaceholder, props: { title: '自助选课', desc: '这里将支持你在开放时段自助选修课程。模块建设中，敬请期待。' } },
            { path: "evaluation", component: StuPlaceholder, props: { title: '教学评教', desc: '这里将支持你对任课教师进行教学评价。模块建设中，敬请期待。' } },
            { path: "exam", component: StuPlaceholder, props: { title: '考试安排', desc: '这里将展示你的考试时间与考场信息。模块建设中，敬请期待。' } },
        ]
    },
    {
        path: "/login",
        component: Login
    }
]

const router = createRouter(
    {
        routes: routes,
        history: createWebHashHistory()
    }
)

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path === '/login') {
        next()
    } else {
        if (!token) {
            next('/login')
        } else {
            const requiredRoles = routeRoleMap[to.path]
            if (requiredRoles) {
                const userStr = localStorage.getItem('user')
                let userRole = ''
                if (userStr) {
                    try {
                        const user = JSON.parse(userStr)
                        userRole = user.role || ''
                    } catch (e) {}
                }
                if (!requiredRoles.includes(userRole)) {
                    ElMessage.warning('权限不足，无法访问该页面')
                    next('/home/t1')
                    return
                }
            }
            next()
        }
    }
})

export default router
