package com.muqarariplus.platform.config;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import com.muqarariplus.platform.entity.SiteContent;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.ExpertRepository;
import com.muqarariplus.platform.repository.SiteContentRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SiteContentRepository siteContentRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          CourseRepository courseRepository,
                          SiteContentRepository siteContentRepository,
                          ExpertRepository expertRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.siteContentRepository = siteContentRepository;
        this.expertRepository = expertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedUsers();
        seedExperts();
        seedCourses();
        seedSiteContent();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────────────────────────────────────
    private void seedUsers() {
        // SUPER ADMIN
        if (userRepository.findByUsername("admin") == null) {
            User superAdmin = new User();
            superAdmin.setUsername("admin");
            superAdmin.setEmail("admin@super.muqarariplus.com");
            superAdmin.setPassword(passwordEncoder.encode("123456_Xy"));
            superAdmin.setFirstName("خالد");
            superAdmin.setLastName("نمر");
            superAdmin.setRole("ROLE_SUPER_ADMIN");
            superAdmin.setStatus("APPROVED");
            userRepository.save(superAdmin);
            System.out.println("SEEDER: SUPER_ADMIN created (Username: admin)");
        }

        // ADMIN
        if (userRepository.findByEmail("sultan@muqarariplus.com") == null) {
            User admin = new User();
            String adminUser = "admin" + String.format("%04d", new java.util.Random().nextInt(10000));
            String adminPass = UUID.randomUUID().toString().substring(0, 8);
            admin.setUsername(adminUser);
            admin.setEmail("sultan@muqarariplus.com");
            admin.setPassword(passwordEncoder.encode(adminPass));
            admin.setFirstName("سلطان");
            admin.setLastName("محمد");
            admin.setRole("ROLE_ADMIN");
            admin.setStatus("APPROVED");
            userRepository.save(admin);
            System.out.println("=================================================");
            System.out.println("SEEDER: ADMIN Auto-Generated Credentials:");
            System.out.println("Name: سلطان محمد");
            System.out.println("Username: " + adminUser);
            System.out.println("Password: " + adminPass);
            System.out.println("=================================================");
        }

        // EXPERT
        if (userRepository.findByEmail("expert@muqarariplus.com") == null) {
            User expert = new User();
            expert.setEmail("expert@muqarariplus.com");
            expert.setPassword(passwordEncoder.encode("expert123"));
            expert.setFirstName("عماد");
            expert.setLastName("خالد");
            expert.setRole("ROLE_EXPERT");
            expert.setStatus("APPROVED");
            userRepository.save(expert);
            System.out.println("SEEDER: EXPERT User created (Email: expert@muqarariplus.com)");
        }

        // STUDENT
        if (userRepository.findByEmail("student@muqarariplus.com") == null) {
            User student = new User();
            student.setEmail("student@muqarariplus.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setFirstName("إبراهيم");
            student.setLastName("عادل");
            student.setRole("ROLE_STUDENT");
            student.setStatus("APPROVED");
            userRepository.save(student);
            System.out.println("SEEDER: STUDENT User created (Email: student@muqarariplus.com)");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXPERTS (Linked entities for ROLE_EXPERT users)
    // ─────────────────────────────────────────────────────────────────────────
    private void seedExperts() {
        User expertUser = userRepository.findByEmail("expert@muqarariplus.com");
        if (expertUser != null && expertRepository.findByUserId(expertUser.getId()).isEmpty()) {
            Expert expert = new Expert();
            expert.setUser(expertUser);
            expert.setStatus(ExpertStatus.NONE);
            expert.setRating(0.0);
            expertRepository.save(expert);
            System.out.println("SEEDER: Expert entity created for expert@muqarariplus.com");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COURSES (73 CCIS courses)
    // ─────────────────────────────────────────────────────────────────────────
    private void seedCourses() {
        if (courseRepository.count() > 0) return;

        String[][] courses = {
            {"IC 101", "Introduction to Islamic Culture", "المدخل إلى الثقافة الإسلامية"},
            {"IC 102", "Islam and Building of Society", "الإسلام وبناء المجتمع"},
            {"IC 103", "Economic System in Islam", "أسس النظام الاقتصادي في الإسلام"},
            {"IC 104", "Political System in Islam", "أسس النظام السياسي في الإسلام"},
            {"ARAB 101", "Language Skills", "المهارات اللغوية"},
            {"ARAB 103", "Arabic Writing", "التحرير العربي"},
            {"COMM 1400", "Communication Skills", "مهارات الاتصال"},
            {"ENG 1210", "Reading Skills", "مهارات القراءة"},
            {"ENG 1220", "Writing Skills", "مهارات الكتابة"},
            {"ENG 1230", "Listening & Speaking Skills", "مهارات الاستماع والمحادثة"},
            {"ENG 1604", "English for Scientific Disciplines", "اللغة الإنجليزية للتخصصات العلمية"},
            {"COMP 1400", "Computer Skills", "مهارات الحاسب"},
            {"MATH 1050", "Differential Calculus", "حساب التفاضل"},
            {"MATH 1060", "Integral Calculus", "حساب التكامل"},
            {"MATH 2220", "Linear Algebra for Computer Students", "الجبر الخطي لطلاب الحاسب"},
            {"STAT 1050", "Probability and Statistics for Computer Students", "الإحصاء والاحتمالات لطلبة الحاسب"},
            {"PHYS 1010", "General Physics 1", "فيزياء عامة (1)"},
            {"PHYS 1040", "General Physics 2", "فيزياء عامة (2)"},
            {"CS 1112", "Discrete Mathematics", "الرياضيات المتقطعة"},
            {"CS 1301", "Computer Programming 1", "برمجة الحاسب 1"},
            {"CS 2301", "Computer Programming 2", "برمجة الحاسب 2"},
            {"CS 3001", "Computing Ethics & Research Methods", "أخلاقيات الحوسبة وطرق البحث"},
            {"CS 3701", "Operating Systems", "نظم التشغيل"},
            {"IS 2511", "Fundamentals of Database Systems", "أساسيات نظم قواعد البيانات"},
            {"CE 1111", "Logic Design", "التصميم المنطقي"},
            {"CE 2121", "Logic Design Lab", "معمل التصميم المنطقي"},
            {"CS 2321", "Data Structures and Algorithms", "الخوارزميات وتراكيب البيانات"},
            {"MATH 3310", "Differential Equations for Computer Students", "المعادلات التفاضلية لطلبة الحاسب"},
            {"CE 2501", "Electrical Circuits", "الدوائر الكهربائية"},
            {"CE 2401", "Computer Organ. & Design", "تنظيم وتصميم الحاسب"},
            {"CE 2511", "Electrical Circuits Lab", "معمل الدوائر الكهربائية"},
            {"CE 3501", "Electronic Circuits", "الدوائر الالكترونية"},
            {"CS 3501", "Introduction to Artificial Intelligence", "مقدمة في الذكاء الاصطناعي"},
            {"CE 3510", "Electronic Circuits Lab", "معمل الدوائر الالكترونية"},
            {"CE 3520", "Signals Analysis & Systems", "تحليل الاشارات والنظم"},
            {"CE 3401", "Computer Architecture", "عمارة الحاسب"},
            {"CE 3270", "Number Theory", "نظرية الأعداد"},
            {"CE 3541", "Control Systems", "انظمة التحكم"},
            {"CE 3701", "Data Communications", "تراسل البيانات"},
            {"SE 2111", "Foundations of Software Engineering", "أسس هندسة البرمجيات"},
            {"SE 4231", "Software Project Management", "إدارة مشروع البرمجيات"},
            {"CE 3711", "Computer Networks", "شبكات الحاسب"},
            {"CE 3601", "Digital Systems Design", "تصميم الأنظمة الرقمية"},
            {"CE 3411", "Microprocessor Systems", "أنظمة المعالجات الدقيقة"},
            {"CE 3551", "Control Systems Lab", "معمل انظمة التحكم"},
            {"CE 3631", "Embedded Systems Design", "تصميم الانظمة المضمنة"},
            {"CE 4501", "VLSI Design", "تصميم انظمة الدوائر المتكاملة"},
            {"CE 4910", "Graduation Project I", "مشروع تخرج 1"},
            {"CE 3721", "Computer Networks Lab", "معمل شبكات الحاسب"},
            {"CE 4801", "Robotics & Automation", "الروبوتية والأتمتة"},
            {"CE 4711", "Computer & Net. Security", "أمن الحاسبات والشبكات"},
            {"CE 4921", "Graduation Project II", "مشروع تخرج 2"},
            {"CE 4901", "Field Training", "تدريب ميداني"},
            {"CE 4981", "Selected Topics", "مواضيع مختارة"},
            {"CE 4991", "Research", "بحث"},
            {"CE 4521", "Digital Signal Processing", "معالجة الإشارات الرقمية"},
            {"CE 4531", "Storage Media Technology", "تقنيات وسائط التخزين"},
            {"CE 4601", "Reconfigurable Computing", "الحوسبة المتشكلة"},
            {"CE 4721", "Sensory Networks", "الشبكات الحسية"},
            {"CE 4740", "Cloud Computing", "الحوسبة السحابية"},
            {"CE 4751", "Multimedia Networking", "شبكات الوسائط المتعددة"},
            {"CE 4760", "Mobile & Wireless Network", "الشبكات اللاسلكية والمتنقلة"},
            {"CS 4201", "Soft Computing", "الحوسبة البرمجية"},
            {"CS 4211", "Simulation and Modeling", "النمذجة والمحاكاة"},
            {"CS 4301", "Compiler Design", "تصميم المترجمات"},
            {"CS 4654", "Digital Image Processing", "معالجة الصور الرقمية"},
            {"CS 4851", "Computer Networks Management", "ادارة شبكات الحاسب"},
            {"SE 4541", "Advanced Software Engineering", "هندسة البرمجيات المتقدمة"},
            {"SE 4551", "Real Time Systems", "أنظمة الوقت الحقيقي"},
            {"CS 4731", "Systems Programming & Administration", "برمجة وإدارة النظم"},
            {"CE 4770", "Internet of Things", "إنترنت الأشياء"},
            {"CE 4561", "Industrial Automation", "الأتمتة الصناعية"},
            {"CE 3421", "High Per. Computing", "الحوسبة عالية الأداء"}
        };

        for (String[] courseData : courses) {
            Course course = new Course();
            course.setCode(courseData[0]);
            course.setNameEn(courseData[1]);
            course.setNameAr(courseData[2]);
            course.setDescriptionEn("CCIS standard course.");
            course.setDescriptionAr("مقرر ضمن كلية علوم الحاسب والمعلومات.");
            course.setUniversity("CCIS");
            courseRepository.save(course);
        }
        System.out.println("SEEDER: All 73 courses seeded successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SITE CONTENT (DB-Driven i18n — replaces messages.properties)
    // ─────────────────────────────────────────────────────────────────────────
    private void seedSiteContent() {
        if (siteContentRepository.count() > 0) return;

        String[][] content = {
            // GLOBAL / LAYOUT
            {"app.title",              "Muqarari+ | Bridge Theory & Practice",          "مقرري+ | اربط بين النظرية والتطبيق"},
            {"app.description",        "Bridge the gap between academic theory and real-world application with Muqarari+.", "سد الفجوة بين النظرية الأكاديمية والتطبيق العملي مع منصة مقرري+."},
            {"nav.logo.alt",           "Muqarari+ Logo",                                "شعار مقرري+"},
            {"nav.courses",            "Courses",                                       "المقررات"},
            {"nav.login",              "Login",                                         "تسجيل الدخول"},
            {"nav.register",           "Register",                                      "إنشاء حساب"},
            {"nav.super_admin",        "Control Center",                                "مركز التحكم الأعلى"},
            {"nav.admin",              "Admin Dashboard",                               "لوحة الإدارة"},
            {"nav.expert",             "Expert Portal",                                 "بوابة الخبراء"},
            {"nav.student",            "My Dashboard",                                  "لوحتي"},
            {"nav.logout",             "Logout",                                        "تسجيل الخروج"},
            {"nav.lang",               "عربي",                                          "English"},
            {"footer.cta",             "Ready to Transform Your Learning Path?",        "هل أنت مستعد لتغيير مسارك التعليمي؟"},
            {"footer.rights",          "© 2026 Muqarari+. All rights reserved.",        "© 2026 مقرري+. جميع الحقوق محفوظة."},

            // HERO
            {"hero.heading",           "Your question \"Why study this?\" — finally answered.", "سؤالك \"ليش أدرس هذا؟\" وجد جوابه أخيراً في \"مقرري+\"."},
            {"hero.sub",               "Bridge the gap between academic theory and real-world practice. Learn directly from certified, practicing industry experts.", "سد الفجوة بين النظرية الأكاديمية والتطبيق العملي. تعلم مباشرةً من خبراء مهنيين ممارسين ومعتمدين."},
            {"hero.cta.courses",       "Find Your Course",                              "ابحث عن مقررك"},
            {"hero.cta.expert",        "Become an Expert",                              "كن خبيراً"},

            // WHY US
            {"why.title",              "Why Muqarari+?",                                "لماذا مقرري+؟"},
            {"why.ai",                 "Generic AI Answers",                            "إجابات ذكاء اصطناعي عامة"},
            {"why.human",              "Real Expert Insights",                          "رؤى خبراء حقيقيين"},

            // PILLARS
            {"pillars.title",          "Our Three Pillars",                             "ركائزنا الأساسية الثلاث"},
            {"pillars.app",            "Practical Applications",                        "التطبيقات العملية"},
            {"pillars.app.desc",       "See exactly how your course material is applied daily in industry environments.", "اكتشف كيف يُطبَّق محتوى مقررك يومياً في بيئات العمل الحقيقية."},
            {"pillars.roadmap",        "Career Roadmap",                                "المسار المهني"},
            {"pillars.roadmap.desc",   "Discover the precise career path from this academic course to a specific job title.", "تعرَّف على المسار المهني المباشر من هذا المقرر الأكاديمي حتى الوظيفة المحددة."},
            {"pillars.res",            "Enrichment Resources",                          "مصادر الإثراء"},
            {"pillars.res.desc",       "Hand-picked books, articles, tools, and certifications recommended by professionals.", "كتب ومقالات وأدوات وشهادات مختارة بعناية بتوصية من المختصين."},

            // BENEFITS
            {"benefits.title",         "Platform Benefits",                             "مزايا المنصة"},
            {"benefits.student",       "For Students: Actionable clarity and deep academic motivation.", "للطلاب: وضوح عملي وتحفيز أكاديمي عميق."},
            {"benefits.univ",          "For Universities: Modern, industry-aligned curriculum enrichment.", "للجامعات: إثراء منهجي حديث يتوافق مع سوق العمل."},
            {"benefits.expert",        "For Experts: Give back to the community and build an elite professional reputation.", "للخبراء: العطاء المجتمعي وبناء سمعة مهنية نخبوية."},

            // LOGIN
            {"login.title",            "Welcome Back",                                  "مرحباً بعودتك"},
            {"login.error",            "Invalid credentials or account is pending verification.", "بيانات الدخول غير صحيحة أو الحساب قيد المراجعة."},
            {"login.success",          "Account created successfully. Please log in.",  "تم إنشاء الحساب بنجاح. يُرجى تسجيل الدخول."},
            {"login.identifier",       "Email Address or Username",                     "البريد الإلكتروني أو اسم المستخدم"},
            {"login.password",         "Password",                                      "كلمة المرور"},
            {"login.submit",           "Login Securely",                                "دخول آمن"},
            {"login.no_account",       "Don't have an account yet?",                    "لا تملك حساباً بعد؟"},

            // REGISTER
            {"register.title",         "Create Your Account",                           "إنشاء حسابك"},
            {"register.fail",          "Registration failed. Please review the errors.", "فشل التسجيل. يُرجى مراجعة الأخطاء."},
            {"register.first_name",    "First Name",                                    "الاسم الأول"},
            {"register.last_name",     "Last Name",                                     "اسم العائلة"},
            {"register.email",         "Email Address",                                 "عنوان البريد الإلكتروني"},
            {"register.password",      "Secure Password",                               "كلمة مرور آمنة"},
            {"register.password_hint", "Minimum 8 characters including uppercase, number, and special character.", "8 أحرف على الأقل تتضمن حرفاً كبيراً ورقماً ورمزاً خاصاً."},
            {"register.role",          "Account Type",                                  "نوع الحساب"},
            {"register.role.student",  "University Student",                            "طالب جامعي"},
            {"register.role.expert",   "Industry Expert",                               "خبير في الصناعة"},
            {"register.submit",        "Register Now",                                  "تسجيل الآن"},
            {"register.have_account",  "Already have an account?",                      "لديك حساب مسبقاً؟"},

            // SUPER ADMIN
            {"superadmin.title",            "Supreme Command Center",                        "مركز القيادة العليا"},
            {"superadmin.subtitle",         "Manage Administrator accounts and their lifecycle.", "إدارة حسابات المدراء ودورة حياتهم في المنصة."},
            {"superadmin.badge",            "SUPER ADMIN",                                   "مدير أعلى"},
            {"superadmin.error",            "An error occurred.",                            "حدث خطأ."},
            {"superadmin.success",          "Administrator Generated Successfully!",          "تم إنشاء حساب المدير بنجاح!"},
            {"superadmin.success_msg",      "Copy these credentials now and store them securely.", "انسخ هذه البيانات الآن واحتفظ بها في مكان آمن."},
            {"superadmin.success_user",     "Username: ",                                    "اسم المستخدم: "},
            {"superadmin.success_pass",     "Password: ",                                    "كلمة المرور: "},
            {"superadmin.create.title",     "Create New Administrator",                      "إنشاء مدير جديد"},
            {"superadmin.create.firstname", "First Name",                                    "الاسم الأول"},
            {"superadmin.create.lastname",  "Last Name",                                     "اسم العائلة"},
            {"superadmin.create.email",     "Email (Backend Record Only)",                   "البريد الإلكتروني (للتتبع فقط)"},
            {"superadmin.create.btn",       "Generate Admin Credentials",                    "توليد بيانات المدير"},
            {"superadmin.fleet.title",      "Active Administrators Fleet",                   "قائمة المدراء النشطين"},
            {"superadmin.fleet.name",       "Admin Name",                                    "اسم المدير"},
            {"superadmin.fleet.email",      "Email",                                         "البريد الإلكتروني"},
            {"superadmin.fleet.username",   "Auto-Generated Username",                       "اسم المستخدم المُولَّد تلقائياً"},
            {"superadmin.fleet.action",     "Actions",                                       "الإجراءات"},
            {"superadmin.fleet.delete",     "Revoke Access",                                 "سحب الصلاحيات"},
            {"superadmin.fleet.delete_confirm", "Are you sure you want to permanently revoke this Administrator's access?", "هل أنت متأكد من رغبتك في سحب صلاحيات هذا المدير نهائياً؟"},
            {"superadmin.fleet.empty",      "No administrators are active in the system.",   "لا يوجد مدراء نشطون في النظام حالياً."},

            // ADMIN
            {"admin.title",              "System Administration",                       "إدارة النظام"},
            {"admin.subtitle",           "Manage expert verifications, users, and platform data.", "إدارة توثيق الخبراء والمستخدمين وبيانات المنصة."},
            {"admin.badge",              "ADMIN",                                       "مدير"},
            {"admin.stat.students",      "Total Students",                              "إجمالي الطلاب"},
            {"admin.stat.experts",       "Verified Experts",                            "خبراء موثَّقون"},
            {"admin.stat.pending",       "Pending Verifications",                       "طلبات التحقق"},
            {"admin.stat.courses",       "Courses Enriched",                            "مقررات مُثرَاة"},
            {"admin.queue.title",        "Expert Verification Queue",                   "قائمة توثيق الخبراء"},
            {"admin.queue.name",         "Expert Name & Email",                         "اسم وبريد الخبير"},
            {"admin.queue.cv",           "CV Document",                                 "السيرة الذاتية"},
            {"admin.queue.cv.btn",       "View CV",                                     "مشاهدة السيرة"},
            {"admin.queue.linkedin",     "LinkedIn Profile",                            "ملف لينكد إن"},
            {"admin.queue.linkedin.btn", "View LinkedIn →",                             "مشاهدة الملف →"},
            {"admin.queue.action",       "Actions",                                     "الإجراءات"},
            {"admin.queue.approve",      "Approve & Verify",                            "قبول وتوثيق"},
            {"admin.queue.reject",       "Reject",                                      "رفض"},
            {"admin.queue.empty",        "No pending experts awaiting verification.",   "لا يوجد خبراء بانتظار التحقق حالياً."},
            {"admin.users.title",        "System User Management",                      "إدارة مستخدمي النظام"},
            {"admin.users.search",       "Search users by email address...",            "ابحث عن مستخدم عبر البريد الإلكتروني..."},
            {"admin.users.email",        "Email Address",                               "عنوان البريد"},
            {"admin.users.name",         "Full Name",                                   "الاسم الكامل"},
            {"admin.users.role",         "Assigned Role",                               "الدور الممنوح"},
            {"admin.users.status",       "Account Status",                              "حالة الحساب"},
            {"admin.users.active",       "Active",                                      "نشط"},
            {"admin.users.pending",      "Pending",                                     "قيد المراجعة"},
            {"admin.health.db",          "Database Health",                             "حالة قاعدة البيانات"},
            {"admin.health.cap",         "25% of Storage Capacity Used",               "25% من سعة التخزين مستخدمة"},
            {"admin.health.server",      "Server Status",                               "حالة الخادم"},
            {"admin.health.uptime",      "Fully Operational (Uptime: 99.9%)",          "يعمل باحترافية (نسبة التشغيل: 99.9%)"},

            // EXPERT PORTAL
            {"expert.title",            "Welcome Back, Expert",                         "مرحباً بعودتك، أيها الخبير"},
            {"expert.subtitle",         "Enrich academic theory with your real-world industry experience.", "أثرِ الجانب الأكاديمي بخبراتك العملية الحقيقية في الصناعة."},
            {"expert.status.title",     "Verification Status",                          "حالة التحقق"},
            {"expert.status.none",      "Not Submitted",                                "لم يتم الإرسال"},
            {"expert.status.pending",   "Pending Verification",                         "قيد التحقق"},
            {"expert.status.approved",  "Verified & Active",                            "موثَّق ونشط"},
            {"expert.status.rejected",  "Rejected",                                     "مرفوض"},
            {"expert.verify.title",     "Identity Verification",                        "التحقق من الهوية"},
            {"expert.verify.desc",      "Upload your professional credentials. Once verified, you can submit enrichments.", "ارفع مستنداتك المهنية. بعد التحقق، ستتمكن من تقديم الإثراءات."},
            {"expert.verify.cv",        "CV / Resume Document (PDF or DOCX)",           "مستند السيرة الذاتية (PDF أو DOCX)"},
            {"expert.verify.cv.hint",   "Accepted formats: PDF, DOC, DOCX. Maximum size: 10MB.", "الصيغ المقبولة: PDF, DOC, DOCX. الحد الأقصى: 10 ميجابايت."},
            {"expert.verify.linkedin",  "Your LinkedIn Profile URL",                    "رابط ملفك الشخصي على لينكد إن"},
            {"expert.verify.btn",       "Submit for Review",                            "إرسال للمراجعة"},
            {"expert.notify.approved.title", "Identity Verified!",                       "تم التحقق من هويتك!"},
            {"expert.notify.approved",  "Your data has been reviewed and your identity has been approved on the platform.", "بياناتك تمت مراجعتها و تم اعتماده في المنصة."},
            {"expert.notify.rejected.title", "Verification Rejected",                    "تم رفض التحقق"},
            {"expert.notify.rejected",  "Your data has been reviewed and the request was rejected.", "بياناتك تمت مراجعتها و رفض الطلب."},
            {"expert.notify.pending.title",  "Under Review",                             "قيد المراجعة"},
            {"expert.notify.pending",   "Your submission is being reviewed by an administrator. You will be notified of the result.", "طلبك قيد المراجعة من قبل المسؤول. سيتم إشعارك بالنتيجة."},
            {"expert.notify.submitted", "Your documents have been submitted successfully for review!", "تم إرسال مستنداتك بنجاح للمراجعة!"},
            {"expert.cooldown.title",   "Cooldown Active",                              "المؤقت نشط"},
            {"expert.cooldown.msg",     "A 5-minute timer has been set since the rejection. Please wait before you can resubmit.", "تم وضع مؤقت مدته 5 دقائق منذ محاولة الرفع الأولى حتى تتمكن من إعادة المحاولة."},
            {"expert.cooldown.note",    "The form will reappear automatically when the timer expires.", "سيظهر النموذج تلقائياً بعد انتهاء المؤقت."},
            {"expert.enrich.title",     "Submit a New Course Enrichment",               "تقديم إثراء جديد لمقرر"},
            {"expert.enrich.locked",    "Locked",                                       "مقفل"},
            {"expert.enrich.locked_msg","This section unlocks after identity verification.", "يُفتح هذا القسم تلقائياً بعد التحقق من هويتك."},
            {"expert.enrich.unlocked_msg","Select a course and share your professional insights.", "اختر مقرراً وشارك رؤيتك المهنية."},
            {"expert.enrich.course",    "Select a University Course",                   "اختر المقرر الجامعي"},
            {"expert.enrich.pillar1",   "Pillar 1: Practical Applications",             "الركيزة الأولى: التطبيقات العملية"},
            {"expert.enrich.hp1",       "How is this topic applied in real industry environments?", "كيف يُطبَّق هذا الموضوع في بيئة العمل الحقيقية؟"},
            {"expert.enrich.pillar2",   "Pillar 2: Career Roadmap",                     "الركيزة الثانية: المسار المهني"},
            {"expert.enrich.hp2",       "What specific career paths does this skill lead to?", "ما هي المسارات الوظيفية التي تقود إليها هذه المهارة؟"},
            {"expert.enrich.btn",       "Submit Contribution",                          "تأكيد المساهمة"},
            {"expert.contrib.title",    "My Contributions",                             "مساهماتي وإثراءاتي"},
            {"expert.contrib.empty",    "No contributions submitted yet.",              "لا توجد مساهمات بعد."},
            {"expert.contrib.empty_msg","Your verified enrichments will appear here.",  "ستظهر إثراءاتك المعتمدة هنا بجانب إحصائيات التفاعل."},

            // STUDENT DASHBOARD
            {"student.badge",           "Student Portal",                               "بوابة الطالب"},
            {"student.title",           "Welcome to Your Learning Path",                "مرحباً بك في مسارك التعليمي"},
            {"student.subtitle",        "Track your enrolled courses and explore the latest industry insights.", "تتبع مقرراتك المسجلة واكتشف أحدث رؤى الخبراء."},
            {"student.search",          "Search by course code or name...",             "ابحث برمز أو اسم المقرر..."},
            {"student.path.title",      "My Enrolled Courses",                          "مقرراتي المسجلة"},
            {"student.path.empty",      "You have not enrolled in any courses yet.",    "لم تقم بالتسجيل في أي مقررات دراسية بعد."},
            {"student.path.empty_msg",  "Start by browsing the course catalog to discover real-world applications.", "ابدأ باستعراض كتالوج المقررات لاكتشاف التطبيقات العملية."},
            {"student.path.btn",        "Browse Course Catalog →",                      "استعرض قائمة المقررات ←"},
            {"student.feed.title",      "Industry Insights Feed",                       "آخر مستجدات الصناعة"},
            {"student.feed.subtitle",   "The latest expert enrichments relevant to your enrolled courses.", "أحدث إثراءات الخبراء ذات الصلة بمقرراتك المسجلة."},
            {"student.feed.empty",      "No industry insights available for your current courses.", "لا توجد رؤى مهنية متاحة لمقرراتك الحالية."},
            {"student.feed.btn",        "Load More Insights",                           "تحميل المزيد"},

            // COURSES PAGE
            {"courses.title",              "Course Catalog | Muqarari+",               "كتالوج المقررات | مقرري+"},
            {"courses.search.placeholder", "Search by course name or code...",          "ابحث باسم أو رمز المقرر..."},
            {"courses.card.resources",     "Expert Resources",                          "موارد الخبراء"},
            {"courses.card.view",          "View Details →",                            "عرض التفاصيل ←"},
            {"courses.empty.title",        "No Courses Found",                          "لا توجد مقررات"},
            {"courses.empty.desc",         "No courses matched your search. Try broadening your criteria.", "لم يتطابق بحثك مع أي مقرر. جرِّب توسيع معايير البحث."},

            // COURSE DETAIL
            {"course.expert.verified",          "Expert-Verified Course",              "مقرر معتمد من الخبراء"},
            {"course.syllabus",                 "View Official Syllabus",              "عرض الخطة الدراسية الرسمية"},
            {"course.details.title",            "Expert Course Enrichment",            "إثراء الخبراء للمقرر"},
            {"course.enrichment.empty_app",     "No practical applications submitted for this course yet.", "لم تُقدَّم تطبيقات عملية لهذا المقرر بعد."},
            {"course.enrichment.empty_roadmap", "No career roadmap submitted for this course yet.", "لم يُقدَّم مسار مهني لهذا المقرر بعد."},
            {"course.enrichment.empty_resources","No enrichment resources submitted for this course yet.", "لم تُضَف مصادر إثرائية لهذا المقرر بعد."},
            {"course.experts.title",            "Contributing Experts",                "الخبراء المساهمون"},
            {"course.experts.linkedin",         "LinkedIn Profile →",                  "الملف الشخصي ←"},
            {"course.experts.empty",            "No contributing experts for this course yet.", "لم يُضَف خبراء مساهمون لهذا المقرر بعد."},
        };

        for (String[] row : content) {
            SiteContent sc = new SiteContent();
            sc.setContentKey(row[0]);
            sc.setValueEn(row[1]);
            sc.setValueAr(row[2]);
            siteContentRepository.save(sc);
        }
        System.out.println("SEEDER: " + content.length + " SiteContent keys seeded into MySQL.");
    }
}
