package com.muqarariplus.platform.config;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SiteContentRepository siteContentRepository;
    private final ExpertRepository expertRepository;
    private final SkillRepository skillRepository;
    private final ToolRepository toolRepository;
    private final ProfessionalCertificateRepository certRepository;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public DatabaseSeeder(UserRepository userRepository, CourseRepository courseRepository,
                          SiteContentRepository siteContentRepository, ExpertRepository expertRepository,
                          SkillRepository skillRepository, ToolRepository toolRepository,
                          ProfessionalCertificateRepository certRepository,
                          CourseEnrichmentRepository enrichmentRepository,
                          PasswordEncoder passwordEncoder, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.siteContentRepository = siteContentRepository;
        this.expertRepository = expertRepository;
        this.skillRepository = skillRepository;
        this.toolRepository = toolRepository;
        this.certRepository = certRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedUsers();
        seedExperts();
        seedCourses();
        seedSiteContent();
        seedIndustryHierarchy();
        seedCertifications();
        purgeNonTechnicalEnrichments();
        List<User> massiveExperts = seedMassiveExperts();
        seedMassiveEnrichments(massiveExperts);
    }

    private void seedUsers() {
        if (userRepository.findByUsername("admin") == null) {
            User u = new User(); u.setUsername("admin"); u.setEmail("admin@super.muqarariplus.com");
            u.setPassword(passwordEncoder.encode("123456_Xy")); u.setFirstName("خالد"); u.setLastName("نمر");
            u.setRole("ROLE_SUPER_ADMIN"); u.setStatus("APPROVED"); userRepository.save(u);
        }
        if (userRepository.findByEmail("sultan@muqarariplus.com") == null) {
            User a = new User(); String au = "admin" + String.format("%04d", new Random().nextInt(10000));
            String ap = UUID.randomUUID().toString().substring(0,8);
            a.setUsername(au); a.setEmail("sultan@muqarariplus.com"); a.setPassword(passwordEncoder.encode(ap));
            a.setFirstName("سلطان"); a.setLastName("محمد"); a.setRole("ROLE_ADMIN"); a.setStatus("APPROVED");
            userRepository.save(a);
            System.out.println("SEEDER ADMIN -> Username: " + au + " Password: " + ap);
        }
        if (userRepository.findByEmail("expert@muqarariplus.com") == null) {
            User e = new User(); e.setEmail("expert@muqarariplus.com"); e.setPassword(passwordEncoder.encode("expert123"));
            e.setFirstName("عماد"); e.setLastName("خالد"); e.setRole("ROLE_EXPERT"); e.setStatus("APPROVED"); userRepository.save(e);
        }
        if (userRepository.findByEmail("student@muqarariplus.com") == null) {
            User s = new User(); s.setEmail("student@muqarariplus.com"); s.setPassword(passwordEncoder.encode("student123"));
            s.setFirstName("إبراهيم"); s.setLastName("عادل"); s.setRole("ROLE_STUDENT"); s.setStatus("APPROVED"); userRepository.save(s);
        }
    }

    private void seedExperts() {
        User eu = userRepository.findByEmail("expert@muqarariplus.com");
        if (eu != null && expertRepository.findByUserId(eu.getId()).isEmpty()) {
            Expert ex = new Expert(); ex.setUser(eu); ex.setStatus(ExpertStatus.NONE); ex.setRating(0.0); expertRepository.save(ex);
        }
    }

    private void seedCourses() {
        // ── NO GLOBAL LOCK — Per-course incremental check by code ──
        String[][] courses = {
            {"IC 101","Introduction to Islamic Culture","المدخل إلى الثقافة الإسلامية"},
            {"IC 102","Islam and Building of Society","الإسلام وبناء المجتمع"},
            {"IC 103","Economic System in Islam","أسس النظام الاقتصادي في الإسلام"},
            {"IC 104","Political System in Islam","أسس النظام السياسي في الإسلام"},
            {"ARAB 101","Language Skills","المهارات اللغوية"},
            {"ARAB 103","Arabic Writing","التحرير العربي"},
            {"COMM 1400","Communication Skills","مهارات الاتصال"},
            {"ENG 1210","Reading Skills","مهارات القراءة"},
            {"ENG 1220","Writing Skills","مهارات الكتابة"},
            {"ENG 1230","Listening & Speaking Skills","مهارات الاستماع والمحادثة"},
            {"ENG 1604","English for Scientific Disciplines","اللغة الإنجليزية للتخصصات العلمية"},
            {"COMP 1400","Computer Skills","مهارات الحاسب"},
            {"MATH 1050","Differential Calculus","حساب التفاضل"},
            {"MATH 1060","Integral Calculus","حساب التكامل"},
            {"MATH 2220","Linear Algebra for Computer Students","الجبر الخطي لطلاب الحاسب"},
            {"STAT 1050","Probability and Statistics for Computer Students","الإحصاء والاحتمالات لطلبة الحاسب"},
            {"PHYS 1010","General Physics 1","فيزياء عامة (1)"},
            {"PHYS 1040","General Physics 2","فيزياء عامة (2)"},
            {"CS 1112","Discrete Mathematics","الرياضيات المتقطعة"},
            {"CS 1301","Computer Programming 1","برمجة الحاسب 1"},
            {"CS 2301","Computer Programming 2","برمجة الحاسب 2"},
            {"CS 3001","Computing Ethics & Research Methods","أخلاقيات الحوسبة وطرق البحث"},
            {"CS 3701","Operating Systems","نظم التشغيل"},
            {"IS 2511","Fundamentals of Database Systems","أساسيات نظم قواعد البيانات"},
            {"CE 1111","Logic Design","التصميم المنطقي"},
            {"CE 2121","Logic Design Lab","معمل التصميم المنطقي"},
            {"CS 2321","Data Structures and Algorithms","الخوارزميات وتراكيب البيانات"},
            {"MATH 3310","Differential Equations for Computer Students","المعادلات التفاضلية لطلبة الحاسب"},
            {"CE 2501","Electrical Circuits","الدوائر الكهربائية"},
            {"CE 2401","Computer Organ. & Design","تنظيم وتصميم الحاسب"},
            {"CE 2511","Electrical Circuits Lab","معمل الدوائر الكهربائية"},
            {"CE 3501","Electronic Circuits","الدوائر الالكترونية"},
            {"CS 3501","Introduction to Artificial Intelligence","مقدمة في الذكاء الاصطناعي"},
            {"CE 3510","Electronic Circuits Lab","معمل الدوائر الالكترونية"},
            {"CE 3520","Signals Analysis & Systems","تحليل الاشارات والنظم"},
            {"CE 3401","Computer Architecture","عمارة الحاسب"},
            {"CE 3270","Number Theory","نظرية الأعداد"},
            {"CE 3541","Control Systems","انظمة التحكم"},
            {"CE 3701","Data Communications","تراسل البيانات"},
            {"SE 2111","Foundations of Software Engineering","أسس هندسة البرمجيات"},
            {"SE 4231","Software Project Management","إدارة مشروع البرمجيات"},
            {"CE 3711","Computer Networks","شبكات الحاسب"},
            {"CE 3601","Digital Systems Design","تصميم الأنظمة الرقمية"},
            {"CE 3411","Microprocessor Systems","أنظمة المعالجات الدقيقة"},
            {"CE 3551","Control Systems Lab","معمل انظمة التحكم"},
            {"CE 3631","Embedded Systems Design","تصميم الانظمة المضمنة"},
            {"CE 4501","VLSI Design","تصميم انظمة الدوائر المتكاملة"},
            {"CE 4910","Graduation Project I","مشروع تخرج 1"},
            {"CE 3721","Computer Networks Lab","معمل شبكات الحاسب"},
            {"CE 4801","Robotics & Automation","الروبوتية والأتمتة"},
            {"CE 4711","Computer & Net. Security","أمن الحاسبات والشبكات"},
            {"CE 4921","Graduation Project II","مشروع تخرج 2"},
            {"CE 4901","Field Training","تدريب ميداني"},
            {"CE 4981","Selected Topics","مواضيع مختارة"},
            {"CE 4991","Research","بحث"},
            {"CE 4521","Digital Signal Processing","معالجة الإشارات الرقمية"},
            {"CE 4531","Storage Media Technology","تقنيات وسائط التخزين"},
            {"CE 4601","Reconfigurable Computing","الحوسبة المتشكلة"},
            {"CE 4721","Sensory Networks","الشبكات الحسية"},
            {"CE 4740","Cloud Computing","الحوسبة السحابية"},
            {"CE 4751","Multimedia Networking","شبكات الوسائط المتعددة"},
            {"CE 4760","Mobile & Wireless Network","الشبكات اللاسلكية والمتنقلة"},
            {"CS 4201","Soft Computing","الحوسبة البرمجية"},
            {"CS 4211","Simulation and Modeling","النمذجة والمحاكاة"},
            {"CS 4301","Compiler Design","تصميم المترجمات"},
            {"CS 4654","Digital Image Processing","معالجة الصور الرقمية"},
            {"CS 4851","Computer Networks Management","ادارة شبكات الحاسب"},
            {"SE 4541","Advanced Software Engineering","هندسة البرمجيات المتقدمة"},
            {"SE 4551","Real Time Systems","أنظمة الوقت الحقيقي"},
            {"CS 4731","Systems Programming & Administration","برمجة وإدارة النظم"},
            {"CE 4770","Internet of Things","إنترنت الأشياء"},
            {"CE 4561","Industrial Automation","الأتمتة الصناعية"},
            {"CE 3421","High Per. Computing","الحوسبة عالية الأداء"}
        };
        int newCourses = 0;
        for (String[] c : courses) {
            if (courseRepository.findByCode(c[0]).isEmpty()) {
                Course co = new Course(); co.setCode(c[0]); co.setNameEn(c[1]); co.setNameAr(c[2]);
                co.setDescriptionEn("CCIS standard course."); co.setDescriptionAr("مقرر ضمن كلية علوم الحاسب والمعلومات.");
                co.setUniversity("CCIS"); courseRepository.save(co);
                newCourses++;
            }
        }
        System.out.println("SEEDER [INCREMENTAL]: " + newCourses + " new courses added (" + (courses.length - newCourses) + " already existed).");
    }

    // ── SITE CONTENT (kept via helper to save space) ─────────────────────
    private void seedSiteContent() {
        // ── NO GLOBAL LOCK — Per-key incremental check ──
        sc("app.title","Muqarari+ | Bridge Theory & Practice","مقرري+ | اربط بين النظرية والتطبيق");
        sc("app.description","Bridge the gap between academic theory and real-world application with Muqarari+.","سد الفجوة بين النظرية الأكاديمية والتطبيق العملي مع منصة مقرري+.");
        sc("nav.logo.alt","Muqarari+ Logo","شعار مقرري+");
        sc("nav.courses","Courses","المقررات");
        sc("nav.login","Login","تسجيل الدخول");
        sc("nav.register","Register","إنشاء حساب");
        sc("nav.super_admin","Control Center","مركز التحكم الأعلى");
        sc("nav.admin","Admin Dashboard","لوحة الإدارة");
        sc("nav.expert","Expert Portal","بوابة الخبراء");
        sc("nav.student","My Dashboard","لوحتي");
        sc("nav.logout","Logout","تسجيل الخروج");
        sc("nav.lang","عربي","English");
        sc("footer.cta","Ready to Transform Your Learning Path?","هل أنت مستعد لتغيير مسارك التعليمي؟");
        sc("footer.rights","© 2026 Muqarari+. All rights reserved.","© 2026 مقرري+. جميع الحقوق محفوظة.");
        sc("hero.heading","Your question \"Why study this?\" — finally answered.","سؤالك \"ليش أدرس هذا؟\" وجد جوابه أخيراً في \"مقرري+\".");
        sc("hero.sub","Bridge the gap between academic theory and real-world practice. Learn directly from certified, practicing industry experts.","سد الفجوة بين النظرية الأكاديمية والتطبيق العملي. تعلم مباشرةً من خبراء مهنيين ممارسين ومعتمدين.");
        sc("hero.cta.courses","Find Your Course","ابحث عن مقررك");
        sc("hero.cta.expert","Become an Expert","كن خبيراً");
        sc("why.title","Why Muqarari+?","لماذا مقرري+؟");
        sc("why.ai","Generic AI Answers","إجابات ذكاء اصطناعي عامة");
        sc("why.human","Real Expert Insights","رؤى خبراء حقيقيين");
        sc("pillars.title","Our Three Pillars","ركائزنا الأساسية الثلاث");
        sc("pillars.app","Practical Applications","التطبيقات العملية");
        sc("pillars.app.desc","See exactly how your course material is applied daily in industry environments.","اكتشف كيف يُطبَّق محتوى مقررك يومياً في بيئات العمل الحقيقية.");
        sc("pillars.roadmap","Career Roadmap","المسار المهني");
        sc("pillars.roadmap.desc","Discover the precise career path from this academic course to a specific job title.","تعرَّف على المسار المهني المباشر من هذا المقرر الأكاديمي حتى الوظيفة المحددة.");
        sc("pillars.res","Enrichment Resources","مصادر الإثراء");
        sc("pillars.res.desc","Hand-picked books, articles, tools, and certifications recommended by professionals.","كتب ومقالات وأدوات وشهادات مختارة بعناية بتوصية من المختصين.");
        sc("benefits.title","Platform Benefits","مزايا المنصة");
        sc("benefits.student","For Students: Actionable clarity and deep academic motivation.","للطلاب: وضوح عملي وتحفيز أكاديمي عميق.");
        sc("benefits.univ","For Universities: Modern, industry-aligned curriculum enrichment.","للجامعات: إثراء منهجي حديث يتوافق مع سوق العمل.");
        sc("benefits.expert","For Experts: Give back to the community and build an elite professional reputation.","للخبراء: العطاء المجتمعي وبناء سمعة مهنية نخبوية.");
        sc("login.title","Welcome Back","مرحباً بعودتك");
        sc("login.error","Invalid credentials or account is pending verification.","بيانات الدخول غير صحيحة أو الحساب قيد المراجعة.");
        sc("login.success","Account created successfully. Please log in.","تم إنشاء الحساب بنجاح. يُرجى تسجيل الدخول.");
        sc("login.identifier","Email Address or Username","البريد الإلكتروني أو اسم المستخدم");
        sc("login.password","Password","كلمة المرور");
        sc("login.submit","Login Securely","دخول آمن");
        sc("login.no_account","Don't have an account yet?","لا تملك حساباً بعد؟");
        sc("register.title","Create Your Account","إنشاء حسابك");
        sc("register.fail","Registration failed. Please review the errors.","فشل التسجيل. يُرجى مراجعة الأخطاء.");
        sc("register.first_name","First Name","الاسم الأول");
        sc("register.last_name","Last Name","اسم العائلة");
        sc("register.email","Email Address","عنوان البريد الإلكتروني");
        sc("register.password","Secure Password","كلمة مرور آمنة");
        sc("register.password_hint","Minimum 8 characters including uppercase, number, and special character.","8 أحرف على الأقل تتضمن حرفاً كبيراً ورقماً ورمزاً خاصاً.");
        sc("register.role","Account Type","نوع الحساب");
        sc("register.role.student","University Student","طالب جامعي");
        sc("register.role.expert","Industry Expert","خبير في الصناعة");
        sc("register.submit","Register Now","تسجيل الآن");
        sc("register.have_account","Already have an account?","لديك حساب مسبقاً؟");
        sc("superadmin.title","Supreme Command Center","مركز القيادة العليا");
        sc("superadmin.subtitle","Manage Administrator accounts and their lifecycle.","إدارة حسابات المدراء ودورة حياتهم في المنصة.");
        sc("superadmin.badge","SUPER ADMIN","مدير أعلى");
        sc("superadmin.error","An error occurred.","حدث خطأ.");
        sc("superadmin.success","Administrator Generated Successfully!","تم إنشاء حساب المدير بنجاح!");
        sc("superadmin.success_msg","Copy these credentials now and store them securely.","انسخ هذه البيانات الآن واحتفظ بها في مكان آمن.");
        sc("superadmin.success_user","Username: ","اسم المستخدم: ");
        sc("superadmin.success_pass","Password: ","كلمة المرور: ");
        sc("superadmin.create.title","Create New Administrator","إنشاء مدير جديد");
        sc("superadmin.create.firstname","First Name","الاسم الأول");
        sc("superadmin.create.lastname","Last Name","اسم العائلة");
        sc("superadmin.create.email","Email (Backend Record Only)","البريد الإلكتروني (للتتبع فقط)");
        sc("superadmin.create.btn","Generate Admin Credentials","توليد بيانات المدير");
        sc("superadmin.fleet.title","Active Administrators Fleet","قائمة المدراء النشطين");
        sc("superadmin.fleet.name","Admin Name","اسم المدير");
        sc("superadmin.fleet.email","Email","البريد الإلكتروني");
        sc("superadmin.fleet.username","Auto-Generated Username","اسم المستخدم المُولَّد تلقائياً");
        sc("superadmin.fleet.action","Actions","الإجراءات");
        sc("superadmin.fleet.delete","Revoke Access","سحب الصلاحيات");
        sc("superadmin.fleet.delete_confirm","Are you sure you want to permanently revoke this Administrator's access?","هل أنت متأكد من رغبتك في سحب صلاحيات هذا المدير نهائياً؟");
        sc("superadmin.fleet.empty","No administrators are active in the system.","لا يوجد مدراء نشطون في النظام حالياً.");
        sc("admin.title","System Administration","إدارة النظام");
        sc("admin.subtitle","Manage expert verifications, users, and platform data.","إدارة توثيق الخبراء والمستخدمين وبيانات المنصة.");
        sc("admin.badge","ADMIN","مدير");
        sc("admin.stat.students","Total Students","إجمالي الطلاب");
        sc("admin.stat.experts","Verified Experts","خبراء موثَّقون");
        sc("admin.stat.pending","Pending Verifications","طلبات التحقق");
        sc("admin.stat.courses","Courses Enriched","مقررات مُثرَاة");
        sc("admin.queue.title","Expert Verification Queue","قائمة توثيق الخبراء");
        sc("admin.queue.name","Expert Name & Email","اسم وبريد الخبير");
        sc("admin.queue.cv","CV Document","السيرة الذاتية");
        sc("admin.queue.cv.btn","View CV","مشاهدة السيرة");
        sc("admin.queue.linkedin","LinkedIn Profile","ملف لينكد إن");
        sc("admin.queue.linkedin.btn","View LinkedIn →","مشاهدة الملف →");
        sc("admin.queue.action","Actions","الإجراءات");
        sc("admin.queue.approve","Approve & Verify","قبول وتوثيق");
        sc("admin.queue.reject","Reject","رفض");
        sc("admin.queue.empty","No pending experts awaiting verification.","لا يوجد خبراء بانتظار التحقق حالياً.");
        sc("admin.users.title","System User Management","إدارة مستخدمي النظام");
        sc("admin.users.search","Search users by email address...","ابحث عن مستخدم عبر البريد الإلكتروني...");
        sc("admin.users.email","Email Address","عنوان البريد");
        sc("admin.users.name","Full Name","الاسم الكامل");
        sc("admin.users.role","Assigned Role","الدور الممنوح");
        sc("admin.users.status","Account Status","حالة الحساب");
        sc("admin.users.active","Active","نشط");
        sc("admin.users.pending","Pending","قيد المراجعة");
        sc("admin.health.db","Database Health","حالة قاعدة البيانات");
        sc("admin.health.cap","25% of Storage Capacity Used","25% من سعة التخزين مستخدمة");
        sc("admin.health.server","Server Status","حالة الخادم");
        sc("admin.health.uptime","Fully Operational (Uptime: 99.9%)","يعمل باحترافية (نسبة التشغيل: 99.9%)");
        sc("expert.title","Welcome Back, Expert","مرحباً بعودتك، أيها الخبير");
        sc("expert.subtitle","Enrich academic theory with your real-world industry experience.","أثرِ الجانب الأكاديمي بخبراتك العملية الحقيقية في الصناعة.");
        sc("expert.status.title","Verification Status","حالة التحقق");
        sc("expert.status.none","Not Submitted","لم يتم الإرسال");
        sc("expert.status.pending","Pending Verification","قيد التحقق");
        sc("expert.status.approved","Verified & Active","موثَّق ونشط");
        sc("expert.status.rejected","Rejected","مرفوض");
        sc("expert.verify.title","Identity Verification","التحقق من الهوية");
        sc("expert.verify.desc","Upload your professional credentials. Once verified, you can submit enrichments.","ارفع مستنداتك المهنية. بعد التحقق، ستتمكن من تقديم الإثراءات.");
        sc("expert.verify.cv","CV / Resume Document (PDF or DOCX)","مستند السيرة الذاتية (PDF أو DOCX)");
        sc("expert.verify.cv.hint","Accepted formats: PDF, DOC, DOCX. Maximum size: 10MB.","الصيغ المقبولة: PDF, DOC, DOCX. الحد الأقصى: 10 ميجابايت.");
        sc("expert.verify.linkedin","Your LinkedIn Profile URL","رابط ملفك الشخصي على لينكد إن");
        sc("expert.verify.btn","Submit for Review","إرسال للمراجعة");
        sc("expert.notify.approved.title","Identity Verified!","تم التحقق من هويتك!");
        sc("expert.notify.approved","Your data has been reviewed and your identity has been approved on the platform.","بياناتك تمت مراجعتها و تم اعتماده في المنصة.");
        sc("expert.notify.rejected.title","Verification Rejected","تم رفض التحقق");
        sc("expert.notify.rejected","Your data has been reviewed and the request was rejected.","بياناتك تمت مراجعتها و رفض الطلب.");
        sc("expert.notify.pending.title","Under Review","قيد المراجعة");
        sc("expert.notify.pending","Your submission is being reviewed by an administrator.","طلبك قيد المراجعة من قبل المسؤول. سيتم إشعارك بالنتيجة.");
        sc("expert.notify.submitted","Your documents have been submitted successfully for review!","تم إرسال مستنداتك بنجاح للمراجعة!");
        sc("expert.cooldown.title","Cooldown Active","المؤقت نشط");
        sc("expert.cooldown.msg","A 5-minute timer has been set since the rejection.","تم وضع مؤقت مدته 5 دقائق.");
        sc("expert.cooldown.note","The form will reappear automatically when the timer expires.","سيظهر النموذج تلقائياً بعد انتهاء المؤقت.");
        sc("expert.enrich.title","Submit a New Course Enrichment","تقديم إثراء جديد لمقرر");
        sc("expert.enrich.locked","Locked","مقفل");
        sc("expert.enrich.locked_msg","This section unlocks after identity verification.","يُفتح هذا القسم تلقائياً بعد التحقق من هويتك.");
        sc("expert.enrich.unlocked_msg","Select a course and share your professional insights.","اختر مقرراً وشارك رؤيتك المهنية.");
        sc("expert.enrich.course","Select a University Course","اختر المقرر الجامعي");
        sc("expert.enrich.pillar1","Pillar 1: Practical Applications","الركيزة الأولى: التطبيقات العملية");
        sc("expert.enrich.hp1","How is this topic applied in real industry environments?","كيف يُطبَّق هذا الموضوع في بيئة العمل الحقيقية؟");
        sc("expert.enrich.pillar2","Pillar 2: Career Roadmap","الركيزة الثانية: المسار المهني");
        sc("expert.enrich.hp2","What specific career paths does this skill lead to?","ما هي المسارات الوظيفية التي تقود إليها هذه المهارة؟");
        sc("expert.enrich.btn","Submit Contribution","تأكيد المساهمة");
        sc("expert.contrib.title","My Contributions","مساهماتي وإثراءاتي");
        sc("expert.contrib.empty","No contributions submitted yet.","لا توجد مساهمات بعد.");
        sc("expert.contrib.empty_msg","Your verified enrichments will appear here.","ستظهر إثراءاتك المعتمدة هنا بجانب إحصائيات التفاعل.");
        sc("student.badge","Student Portal","بوابة الطالب");
        sc("student.title","Welcome to Your Learning Path","مرحباً بك في مسارك التعليمي");
        sc("student.subtitle","Track your enrolled courses and explore the latest industry insights.","تتبع مقرراتك المسجلة واكتشف أحدث رؤى الخبراء.");
        sc("student.search","Search by course code or name...","ابحث برمز أو اسم المقرر...");
        sc("student.path.title","My Enrolled Courses","مقرراتي المسجلة");
        sc("student.path.empty","You have not enrolled in any courses yet.","لم تقم بالتسجيل في أي مقررات دراسية بعد.");
        sc("student.path.empty_msg","Start by browsing the course catalog.","ابدأ باستعراض كتالوج المقررات.");
        sc("student.path.btn","Browse Course Catalog →","استعرض قائمة المقررات ←");
        sc("student.feed.title","Industry Insights Feed","آخر مستجدات الصناعة");
        sc("student.feed.subtitle","The latest expert enrichments relevant to your enrolled courses.","أحدث إثراءات الخبراء ذات الصلة بمقرراتك المسجلة.");
        sc("student.feed.empty","No industry insights available for your current courses.","لا توجد رؤى مهنية متاحة لمقرراتك الحالية.");
        sc("student.feed.btn","Load More Insights","تحميل المزيد");
        sc("courses.title","Course Catalog | Muqarari+","كتالوج المقررات | مقرري+");
        sc("courses.search.placeholder","Search by course name or code...","ابحث باسم أو رمز المقرر...");
        sc("courses.card.resources","Expert Resources","موارد الخبراء");
        sc("courses.card.view","View Details →","عرض التفاصيل ←");
        sc("courses.empty.title","No Courses Found","لا توجد مقررات");
        sc("courses.empty.desc","No courses matched your search.","لم يتطابق بحثك مع أي مقرر.");
        sc("course.expert.verified","Expert-Verified Course","مقرر معتمد من الخبراء");
        sc("course.syllabus","View Official Syllabus","عرض الخطة الدراسية الرسمية");
        sc("course.details.title","Expert Course Enrichment","إثراء الخبراء للمقرر");
        sc("course.enrichment.empty_app","No practical applications submitted yet.","لم تُقدَّم تطبيقات عملية لهذا المقرر بعد.");
        sc("course.enrichment.empty_roadmap","No career roadmap submitted yet.","لم يُقدَّم مسار مهني لهذا المقرر بعد.");
        sc("course.enrichment.empty_resources","No enrichment resources submitted yet.","لم تُضَف مصادر إثرائية لهذا المقرر بعد.");
        sc("course.experts.title","Contributing Experts","الخبراء المساهمون");
        sc("course.experts.linkedin","LinkedIn Profile →","الملف الشخصي ←");
        sc("course.experts.empty","No contributing experts yet.","لم يُضَف خبراء مساهمون لهذا المقرر بعد.");
        // ── Super Admin Unified Command Center i18n ──
        sc("sa.title","Supreme Command Center","مركز القيادة العليا");
        sc("sa.subtitle","Full platform oversight, analytics, and control.","إشراف كامل على المنصة والتحليلات والتحكم.");
        sc("sa.badge","SUPER ADMIN","مدير أعلى");
        sc("sa.tab.dashboard","Dashboard","لوحة التحكم");
        sc("sa.tab.users","User Matrix","مصفوفة المستخدمين");
        sc("sa.tab.admin","Admin Tools","أدوات الإدارة");
        sc("sa.tab.generate","Generate Admins","توليد المدراء");
        sc("sa.kpi.total_users","Total Users","إجمالي المستخدمين");
        sc("sa.kpi.experts","Registered Experts","الخبراء المسجلين");
        sc("sa.kpi.courses","Total Courses","المقررات الدراسية");
        sc("sa.kpi.enrichments","Total Enrichments","إجمالي الإثراءات");
        sc("sa.kpi.total_users_desc","All registered accounts on the platform","جميع الحسابات المسجلة في المنصة");
        sc("sa.kpi.experts_desc","Expert accounts registered","حسابات الخبراء المسجلين");
        sc("sa.kpi.courses_desc","Courses in the academic catalog","المقررات في الكتالوج الأكاديمي");
        sc("sa.kpi.enrichments_desc","Total expert contributions","إجمالي مساهمات الخبراء");
        sc("sa.chart.users","User Distribution by Role","توزيع المستخدمين حسب الدور");
        sc("sa.chart.enrichments","Enrichment Status","حالة الإثراءات");
        sc("sa.chart.live","LIVE","مباشر");
        sc("sa.chart.frequency","Frequency","التكرار");
        sc("sa.audit.title","Live Audit Trail","سجل المراقبة الحي");
        sc("sa.audit.recording","RECORDING","تسجيل");
        sc("sa.audit.latest","Latest 50 entries","آخر 50 إدخال");
        sc("sa.audit.actor","Actor","الفاعل");
        sc("sa.audit.role","Role","الدور");
        sc("sa.audit.action","Action","الإجراء");
        sc("sa.audit.entity","Entity","الكيان");
        sc("sa.audit.id","ID","المعرف");
        sc("sa.audit.timestamp","Timestamp","الطابع الزمني");
        sc("sa.audit.details","Details","التفاصيل");
        sc("sa.audit.view","View","عرض");
        sc("sa.audit.empty","No audit entries recorded yet.","لا توجد سجلات مراقبة حتى الآن.");
        sc("sa.users.title","User Matrix","مصفوفة المستخدمين");
        sc("sa.users.subtitle","Full account control and role management.","تحكم كامل بالحسابات وإدارة الأدوار.");
        sc("sa.users.search","Search by name, email, role...","ابحث بالاسم أو البريد أو الدور...");
        sc("sa.users.id","ID","المعرف");
        sc("sa.users.name","Name","الاسم");
        sc("sa.users.email","Email","البريد الإلكتروني");
        sc("sa.users.role","Role","الدور");
        sc("sa.users.status","Status","الحالة");
        sc("sa.users.joined","Joined","الانضمام");
        sc("sa.users.actions","Actions","الإجراءات");
        sc("sa.users.change_role","Change Role","تغيير الدور");
        sc("sa.users.suspend","Suspend","تعليق");
        sc("sa.users.activate","Activate","تفعيل");
        sc("sa.users.protected","PROTECTED","محمي");
        sc("sa.users.empty","No users found in the database.","لم يتم العثور على مستخدمين في قاعدة البيانات.");
        sc("sa.users.showing","Showing","عرض");
        sc("sa.users.of","of","من");
        sc("sa.users.total","users","مستخدم");
        sc("sa.users.processing","Processing...","جارٍ المعالجة...");
        sc("sa.toast.success","Action Complete","تمت العملية");
        sc("sa.toast.error","Error","خطأ");
        sc("sa.toast.status_updated","Status Updated","تم تحديث الحالة");
        sc("sa.toast.role_updated","Role Updated","تم تحديث الدور");
        sc("sa.toast.network_error","Network error. Please try again.","خطأ في الشبكة. يرجى المحاولة مرة أخرى.");
        System.out.println("SEEDER: SiteContent seeded.");
    }
    private void sc(String key, String en, String ar) {
        if (siteContentRepository.findByContentKey(key).isEmpty()) {
            SiteContent s = new SiteContent(); s.setContentKey(key); s.setValueEn(en); s.setValueAr(ar); siteContentRepository.save(s);
        }
    }

    private void seedIndustryHierarchy() {
        // ── NO GLOBAL LOCK — Per-item incremental check by nameEn ──
        String[][] skills = {
            {"Object-Oriented Programming","البرمجة كائنية التوجه"},{"Agile Methodology","منهجية أجايل"},
            {"Data Structures","هياكل البيانات"},{"Database Management","إدارة قواعد البيانات"},
            {"RESTful API Design","تصميم واجهات برمجة التطبيقات"},{"Cloud Architecture","هندسة الحوسبة السحابية"},
            {"Machine Learning","تعلم الآلة"},{"Cybersecurity Fundamentals","أساسيات الأمن السيبراني"},
            {"DevOps Practices","ممارسات DevOps"},{"Software Testing & QA","اختبار البرمجيات وضمان الجودة"},
            {"Deep Learning","التعلم العميق"},{"Penetration Testing","اختبار الاختراق"},
            {"Microservices Architecture","هندسة الخدمات المصغرة"},{"Mobile Development","تطوير تطبيقات الجوال"},
            {"Project Management","إدارة المشاريع"},{"Risk Management","إدارة المخاطر"},
            {"CI/CD Pipelines","خطوط التكامل والنشر المستمر"},{"Linux Administration","إدارة لينكس"},
            {"Frontend Development","تطوير الواجهات الأمامية"},{"Cryptography","التشفير"},
            {"Natural Language Processing","معالجة اللغات الطبيعية"},{"Computer Vision","الرؤية الحاسوبية"},
            {"Data Mining","التنقيب في البيانات"},{"Network Routing & Switching","التوجيه والتبديل الشبكي"},
            {"SQL Optimization","تحسين استعلامات SQL"},{"Embedded Systems Programming","برمجة الأنظمة المضمنة"},
            {"Digital Circuit Design","تصميم الدوائر الرقمية"},{"Control Theory","نظرية التحكم"},
            {"Signal Processing","معالجة الإشارات"},{"FPGA Design","تصميم FPGA"},
            // ── v1.24: AI/DATA Skills ──
            {"MLOps","عمليات تعلم الآلة"},{"LLM Fine-tuning","ضبط نماذج اللغة الكبيرة"},
            {"Retrieval-Augmented Generation","التوليد المعزز بالاسترجاع"},{"Prompt Engineering","هندسة الأوامر"},
            {"Feature Engineering","هندسة الميزات"},{"Time Series Analysis","تحليل السلاسل الزمنية"},
            {"Reinforcement Learning","التعلم المعزز"},{"Generative AI","الذكاء الاصطناعي التوليدي"},
            {"Transfer Learning","التعلم الانتقالي"},{"Data Lakehouse Architecture","هندسة بحيرة البيانات"},
            {"ETL Pipeline Design","تصميم خطوط ETL"},{"Statistical Modeling","النمذجة الإحصائية"},
            {"Big Data Analytics","تحليلات البيانات الضخمة"},{"Edge AI","ذكاء اصطناعي طرفي"},
            {"Federated Learning","التعلم الموحد"},{"Model Optimization","تحسين النماذج"},
            {"Data Governance","حوكمة البيانات"},{"Anomaly Detection","كشف الشذوذ"},
            {"Recommendation Systems","أنظمة التوصية"},{"Speech Recognition","التعرف على الكلام"},
            {"Sentiment Analysis","تحليل المشاعر"},{"Knowledge Graphs","الرسوم البيانية المعرفية"},
            {"AutoML","التعلم الآلي التلقائي"},{"Neural Architecture Search","بحث البنية العصبية"},
            // ── v1.24: SECURITY Skills ──
            {"Zero Trust Architecture","بنية الثقة الصفرية"},{"SOC Analysis","تحليل مركز العمليات الأمنية"},
            {"Malware Reverse Engineering","الهندسة العكسية للبرمجيات الخبيثة"},{"Incident Response","الاستجابة للحوادث"},
            {"Threat Intelligence","استخبارات التهديدات"},{"Digital Forensics","الطب الشرعي الرقمي"},
            {"Vulnerability Assessment","تقييم الثغرات"},{"Security Architecture","هندسة الأمن"},
            {"Network Security","أمن الشبكات"},{"Application Security","أمن التطبيقات"},
            {"Cloud Security","أمن السحابة"},{"Identity & Access Management","إدارة الهوية والوصول"},
            {"SIEM Management","إدارة SIEM"},{"Red Team Operations","عمليات الفريق الأحمر"},
            {"Blue Team Defense","دفاع الفريق الأزرق"},{"Threat Hunting","تعقب التهديدات"},
            {"Security Automation","أتمتة الأمن"},{"Container Security","أمن الحاويات"},
            {"API Security","أمن واجهات البرمجة"},{"Security Governance","حوكمة الأمن"},
            // ── v1.24: SOFTWARE Skills ──
            {"Domain-Driven Design","التصميم الموجه بالنطاق"},{"GraphQL API Development","تطوير واجهات GraphQL"},
            {"WebSocket Programming","برمجة WebSocket"},{"Test-Driven Development","التطوير الموجه بالاختبارات"},
            {"Clean Architecture","البنية النظيفة"},{"SOLID Principles","مبادئ SOLID"},
            {"Design Patterns","أنماط التصميم"},{"Event-Driven Architecture","البنية الموجهة بالأحداث"},
            {"Serverless Architecture","البنية بدون خوادم"},{"API Gateway Design","تصميم بوابات API"},
            {"Performance Optimization","تحسين الأداء"},{"System Design","تصميم النظم"},
            {"Distributed Systems","الأنظمة الموزعة"},{"Concurrency Programming","البرمجة المتزامنة"},
            {"Infrastructure as Code","البنية التحتية ككود"},{"Site Reliability Engineering","هندسة موثوقية المواقع"},
            // ── v1.24: DATABASE Skills ──
            {"Database Design","تصميم قواعد البيانات"},{"Data Modeling","نمذجة البيانات"},
            {"Query Performance Tuning","ضبط أداء الاستعلامات"},{"Data Warehousing","تخزين البيانات"},
            {"NoSQL Design Patterns","أنماط تصميم NoSQL"},{"Database Migration","ترحيل قواعد البيانات"},
            {"Replication & Sharding","النسخ والتقسيم"},{"Transaction Management","إدارة المعاملات"},
            {"Schema Design","تصميم المخططات"},{"Database Monitoring","مراقبة قواعد البيانات"},
            // ── v1.24: NETWORKS Skills ──
            {"SDN Architecture","هندسة الشبكات المعرفة بالبرمجيات"},{"Network Automation","أتمتة الشبكات"},
            {"Load Balancing","موازنة الأحمال"},{"Network Monitoring","مراقبة الشبكات"},
            {"Cloud Migration","الهجرة السحابية"},{"Multi-Cloud Strategy","استراتيجية السحابة المتعددة"},
            {"Edge Computing","الحوسبة الطرفية"},{"CDN Architecture","هندسة شبكات توصيل المحتوى"},
            {"Network Virtualization","المحاكاة الافتراضية للشبكات"},{"BGP Routing","توجيه BGP"},
            // ── v1.24: HARDWARE Skills ──
            {"PCB Design","تصميم لوحات الدوائر المطبوعة"},{"Microcontroller Programming","برمجة المتحكمات الدقيقة"},
            {"RTOS Development","تطوير أنظمة الوقت الحقيقي"},{"Hardware Description Languages","لغات وصف العتاد"},
            {"SoC Design","تصميم النظام على شريحة"},{"Sensor Integration","تكامل المستشعرات"},
            {"IoT Protocols","بروتوكولات إنترنت الأشياء"},{"PLC Programming","برمجة المتحكم المنطقي"},
            {"Robotics Programming","برمجة الروبوتات"},{"ASIC Design","تصميم الدوائر المتكاملة المخصصة"},
            // ── v1.24: MATH Skills ──
            {"Numerical Methods","الطرق العددية"},{"Optimization Theory","نظرية التحسين"},
            {"Mathematical Modeling","النمذجة الرياضية"},{"Graph Theory","نظرية الرسوم البيانية"},
            {"Probability Theory","نظرية الاحتمالات"},{"Operations Research","بحوث العمليات"},
            {"Stochastic Processes","العمليات العشوائية"},{"Cryptographic Mathematics","الرياضيات التشفيرية"}
        };
        int newSkills = 0;
        for (String[] s : skills) {
            if (skillRepository.findByNameEn(s[0]).isEmpty()) {
                Skill sk = new Skill(); sk.setNameEn(s[0]); sk.setNameAr(s[1]); skillRepository.save(sk);
                newSkills++;
            }
        }
        System.out.println("SEEDER [INCREMENTAL]: " + newSkills + " new Skills added.");

        String[][] tools = {
            {"Docker","دوكر"},{"Git","جِت"},{"Spring Boot","سبرنج بوت"},{"React","رياكت"},
            {"Kubernetes","كوبرنيتيز"},{"PostgreSQL","بوستقريسكيول"},{"Jenkins","جنكنز"},
            {"IntelliJ IDEA","إنتلي جي آيديا"},{"Postman","بوستمان"},{"AWS","أمازون ويب سيرفسز"},
            {"TensorFlow","تنسرفلو"},{"Python","بايثون"},{"Flutter","فلاتر"},{"Jira","جيرا"},
            {"MySQL","ماي إس كيو إل"},{"Linux","لينكس"},{"Wireshark","وايرشارك"},
            {"Azure","مايكروسوفت أزور"},{"Figma","فيجما"},{"Selenium","سيلينيوم"},
            {"PyTorch","باي تورش"},{"Hadoop","هادوب"},{"Power BI","باور بي آي"},
            {"Kali Linux","كالي لينكس"},{"Metasploit","ميتاسبلويت"},{"Burp Suite","بيرب سويت"},
            {"Cisco Packet Tracer","سيسكو باكت تريسر"},{"Terraform","تيرافورم"},
            {"MongoDB","مونغو دي بي"},{"Oracle Database","أوراكل"},{"MATLAB","ماتلاب"},
            {"Arduino","أردوينو"},{"Xilinx Vivado","زايلنكس فيفادو"},{"Simulink","سيميولينك"},
            // ── v1.24: AI/DATA Tools ──
            {"Snowflake","سنوفليك"},{"Databricks","داتابريكس"},{"HuggingFace Transformers","هجينغ فيس"},
            {"MLflow","إم إل فلو"},{"Apache Spark","أباتشي سبارك"},{"Jupyter Notebook","جوبيتر نوتبوك"},
            {"Pandas","بانداز"},{"NumPy","نمباي"},{"Scikit-learn","سايكت ليرن"},
            {"Keras","كيراس"},{"Apache Airflow","أباتشي إيرفلو"},{"LangChain","لانغ تشين"},
            {"OpenCV","أوبن سي في"},{"Tableau","تابلو"},{"Amazon SageMaker","أمازون سيج ميكر"},
            {"Azure ML Studio","أزور إم إل ستوديو"},{"Google Colab","جوجل كولاب"},
            {"Weights & Biases","ويتس آند بايسز"},{"DVC","دي في سي"},
            // ── v1.24: SECURITY Tools ──
            {"CrowdStrike","كراود سترايك"},{"Splunk","سبلنك"},{"Fortinet FortiGate","فورتينت فورتي جيت"},
            {"Nmap","إن ماب"},{"Snort","سنورت"},{"Nessus","نيسس"},
            {"OWASP ZAP","أوواسب زاب"},{"Qualys","كواليس"},{"Palo Alto Networks","بالو ألتو"},
            {"HashiCorp Vault","هاشي كورب فولت"},{"Elastic SIEM","إلاستيك سيم"},
            {"IBM QRadar","آي بي إم كيو ريدار"},{"SentinelOne","سنتينل ون"},
            {"Ghidra","غيدرا"},{"OpenVAS","أوبن فاس"},{"Suricata","سوريكاتا"},
            // ── v1.24: SOFTWARE Tools ──
            {"Apache Kafka","أباتشي كافكا"},{"Redis","ريدس"},{"Next.js","نكست جي إس"},
            {"GraphQL","غراف كيو إل"},{"VS Code","في إس كود"},{"GitHub Actions","غيت هب أكشنز"},
            {"SonarQube","سونار كيوب"},{"Gradle","غرادل"},{"Maven","مافن"},
            {"Nginx","إنجن إكس"},{"RabbitMQ","رابت إم كيو"},{"Prometheus","بروميثيوس"},
            {"Grafana","غرافانا"},{"Swagger","سواغر"},{"ArgoCD","أرغو سي دي"},
            // ── v1.24: DATABASE Tools ──
            {"Apache Cassandra","أباتشي كاساندرا"},{"Neo4j","نيو فور جي"},{"Amazon RDS","أمازون آر دي إس"},
            {"Elasticsearch","إلاستيك سيرش"},{"MariaDB","ماريا دي بي"},{"DynamoDB","داينامو دي بي"},
            {"InfluxDB","إنفلكس دي بي"},{"pgAdmin","بي جي أدمن"},{"DataGrip","داتا غريب"},
            {"Liquibase","لكوي بيس"},{"Oracle RAC","أوراكل راك"},
            // ── v1.24: NETWORKS Tools ──
            {"GNS3","جي إن إس 3"},{"Nagios","ناجيوس"},{"Zabbix","زابيكس"},
            {"Ansible","أنسيبل"},{"CloudFormation","كلاود فورميشن"},{"Istio","إستيو"},
            {"Cloudflare","كلاود فلير"},{"OpenStack","أوبن ستاك"},{"VMware vSphere","في إم وير"},
            {"AWS CloudWatch","أمازون كلاود واتش"},{"Envoy Proxy","إنفوي بروكسي"},
            // ── v1.24: HARDWARE Tools ──
            {"Altium Designer","ألتيوم ديزاينر"},{"KiCad","كي كاد"},{"STM32CubeIDE","إس تي إم كيوب"},
            {"Quartus Prime","كوارتس برايم"},{"LTSpice","إل تي سبايس"},{"LabVIEW","لاب فيو"},
            {"Proteus","بروتيوس"},{"PlatformIO","بلاتفورم آي أو"},{"FreeRTOS","فري آر تي أو إس"}
        };
        int newTools = 0;
        for (String[] t : tools) {
            if (toolRepository.findByNameEn(t[0]).isEmpty()) {
                Tool to = new Tool(); to.setNameEn(t[0]); to.setNameAr(t[1]); toolRepository.save(to);
                newTools++;
            }
        }
        System.out.println("SEEDER [INCREMENTAL]: " + newTools + " new Tools added.");
    }

    private void seedCertifications() {
        // ── NO GLOBAL LOCK — Per-cert incremental check by nameEn ──
        String[][] certs = {
            {"AWS Certified Solutions Architect","مهندس حلول AWS معتمد","AWS"},
            {"AWS Certified Developer","مطور AWS معتمد","AWS"},
            {"Oracle Database SQL Certified Associate","شهادة أوراكل لقواعد البيانات","Oracle"},
            {"CISSP - Certified Information Systems Security Professional","محترف أمن نظم المعلومات المعتمد","ISC2"},
            {"PMP - Project Management Professional","محترف إدارة المشاريع","PMI"},
            {"Google Cloud Professional Data Engineer","مهندس بيانات جوجل كلاود","Google"},
            {"Microsoft Certified: Azure AI Engineer","مهندس ذكاء اصطناعي أزور معتمد","Microsoft"},
            {"Certified ScrumMaster (CSM)","سكرم ماستر معتمد","Scrum Alliance"},
            {"CompTIA Security+","كومبتيا سيكيوريتي بلس","CompTIA"},
            {"Certified Kubernetes Administrator (CKA)","مدير كوبرنيتيز معتمد","CNCF"},
            {"Google Associate Cloud Engineer","مهندس سحابة جوجل المشارك","Google"},
            {"Cisco CCNA","شبكات سيسكو المعتمدة","Cisco"},
            {"ISTQB Certified Tester","اختبار برمجيات ISTQB","ISTQB"},
            {"Terraform Associate","تيرافورم المشارك","HashiCorp"},
            {"Meta Certified Digital Marketing Associate","تسويق رقمي ميتا المعتمد","Meta"},
            {"CEH - Certified Ethical Hacker","الهاكر الأخلاقي المعتمد","EC-Council"},
            {"AWS Certified Data Analytics","محلل بيانات AWS معتمد","AWS"},
            {"IBM Data Science Professional","محترف علوم البيانات IBM","IBM"},
            {"Oracle Certified Professional","محترف أوراكل معتمد","Oracle"},
            {"MongoDB Developer Certification","شهادة مطور مونغو دي بي","MongoDB Inc."},
            {"Cisco CCNP","شبكات سيسكو المتقدمة","Cisco"},
            // ── v1.24: AI/DATA Certifications ──
            {"AWS Machine Learning Specialty","شهادة تخصص تعلم الآلة AWS","AWS"},
            {"Databricks Certified Data Engineer","مهندس بيانات Databricks معتمد","Databricks"},
            {"TensorFlow Developer Certificate","شهادة مطور TensorFlow","Google"},
            {"Google Professional ML Engineer","مهندس تعلم آلة محترف جوجل","Google"},
            {"Snowflake SnowPro Core","شهادة Snowflake SnowPro","Snowflake"},
            {"Azure Data Scientist Associate","عالم بيانات أزور مشارك","Microsoft"},
            {"Azure Data Engineer Associate","مهندس بيانات أزور مشارك","Microsoft"},
            {"Cloudera Data Platform Generalist","شهادة منصة Cloudera","Cloudera"},
            {"SAS Certified Data Scientist","عالم بيانات SAS معتمد","SAS"},
            {"Deep Learning Specialization","شهادة تخصص التعلم العميق","DeepLearning.AI"},
            // ── v1.24: SECURITY Certifications ──
            {"CISM - Certified Information Security Manager","مدير أمن معلومات معتمد","ISACA"},
            {"OSCP - Offensive Security Certified Professional","محترف أمن هجومي معتمد","OffSec"},
            {"GCIH - GIAC Incident Handler","معالج حوادث GIAC","GIAC"},
            {"CompTIA CASP+","كومبتيا CASP+","CompTIA"},
            {"GPEN - GIAC Penetration Tester","مختبر اختراق GIAC","GIAC"},
            {"GSEC - GIAC Security Essentials","أساسيات أمن GIAC","GIAC"},
            {"CCSP - Certified Cloud Security Professional","محترف أمن سحابي معتمد","ISC2"},
            {"CySA+ - CompTIA Cybersecurity Analyst","محلل أمن سيبراني CompTIA","CompTIA"},
            {"CISA - Certified Information Systems Auditor","مدقق نظم معلومات معتمد","ISACA"},
            {"CHFI - Computer Hacking Forensic Investigator","محقق جنائي رقمي","EC-Council"},
            {"CRISC - Certified in Risk and IS Control","معتمد في مخاطر نظم المعلومات","ISACA"},
            {"ISO 27001 Lead Auditor","مدقق رئيسي آيزو 27001","ISO"},
            // ── v1.24: SOFTWARE Certifications ──
            {"Spring Certified Professional","محترف Spring معتمد","VMware"},
            {"AWS DevOps Engineer Professional","مهندس DevOps AWS محترف","AWS"},
            {"CKAD - Certified Kubernetes App Developer","مطور تطبيقات كوبرنيتيز معتمد","CNCF"},
            {"PMI-ACP - Agile Certified Practitioner","ممارس أجايل PMI معتمد","PMI"},
            {"Oracle Java SE Developer","مطور جافا SE أوراكل","Oracle"},
            {"Red Hat Certified Engineer","مهندس Red Hat معتمد","Red Hat"},
            {"Azure DevOps Engineer Expert","خبير DevOps أزور","Microsoft"},
            {"GitHub Actions Certification","شهادة GitHub Actions","GitHub"},
            {"Docker Certified Associate","مساعد Docker معتمد","Docker"},
            {"SAFe Agilist","قائد SAFe أجايل","Scaled Agile"},
            // ── v1.24: DATABASE Certifications ──
            {"AWS Database Specialty","شهادة تخصص قواعد البيانات AWS","AWS"},
            {"Azure Database Administrator","مدير قواعد بيانات أزور","Microsoft"},
            {"PostgreSQL Certified Associate","شهادة PostgreSQL معتمدة","EnterpriseDB"},
            {"Cassandra Administrator Certification","شهادة مدير Cassandra","DataStax"},
            {"Redis Certified Developer","مطور Redis معتمد","Redis Labs"},
            {"MariaDB Certified DBA","مدير قواعد بيانات MariaDB معتمد","MariaDB"},
            {"Neo4j Certified Professional","محترف Neo4j معتمد","Neo4j"},
            // ── v1.24: NETWORKS Certifications ──
            {"AWS Advanced Networking Specialty","شهادة شبكات AWS المتقدمة","AWS"},
            {"Azure Solutions Architect Expert","مهندس حلول أزور خبير","Microsoft"},
            {"Azure Network Engineer Associate","مهندس شبكات أزور مشارك","Microsoft"},
            {"CompTIA Network+","كومبتيا نتوورك+","CompTIA"},
            {"Juniper JNCIA","شهادة شبكات جونيبر الأساسية","Juniper"},
            {"VMware VCP-DCV","محترف VMware لمراكز البيانات","VMware"},
            // ── v1.24: HARDWARE Certifications ──
            {"CompTIA A+","كومبتيا A+","CompTIA"},
            {"ARM Accredited Engineer","مهندس ARM معتمد","ARM"}
        };
        int newCerts = 0;
        for (String[] c : certs) {
            if (certRepository.findByNameEn(c[0]).isEmpty()) {
                ProfessionalCertificate pc = new ProfessionalCertificate(); pc.setNameEn(c[0]); pc.setNameAr(c[1]); pc.setIssuingBody(c[2]);
                certRepository.save(pc);
                newCerts++;
            }
        }
        System.out.println("SEEDER [INCREMENTAL]: " + newCerts + " new Certifications added.");
    }

    // ═════════════════════════════════════════════════════════════════════
    // PHASE 1: PURGE NON-TECHNICAL COURSE ENRICHMENTS
    // ═════════════════════════════════════════════════════════════════════
    private static final String[] NON_TECH_KEYWORDS = {
        "ثقافة", "سلم", "إسلام", "عرب", "لغة", "مهارات", "قرآن", "تحرير",
        "islamic", "arabic", "english", "communication", "writing", "reading"
    };

    private boolean isNonTechnicalCourse(Course course) {
        String combined = (course.getNameEn() + " " + course.getNameAr()).toLowerCase();
        for (String kw : NON_TECH_KEYWORDS) {
            if (combined.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private void purgeNonTechnicalEnrichments() {
        List<Course> allCourses = courseRepository.findAll();
        int purgedNonTech = 0;
        int purgedStale = 0;
        for (Course course : allCourses) {
            if (isNonTechnicalCourse(course)) {
                // Phase 1A: RUTHLESS PURGE — delete ALL enrichments (ANY status) from non-technical courses
                List<CourseEnrichment> allEnrichments = enrichmentRepository.findByCourseId(course.getId());
                if (!allEnrichments.isEmpty()) {
                    enrichmentRepository.deleteAll(allEnrichments);
                    purgedNonTech += allEnrichments.size();
                }
                continue;
            }

            List<CourseEnrichment> approved = enrichmentRepository
                .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED);
            if (approved.isEmpty()) continue;

            // Phase 1B: Purge contaminated enrichments from technical courses
            String domain = classifyCourse(course);
            List<String> validCertNames = getCertsForDomain(domain);
            List<String> validToolNames = getToolsForDomain(domain);
            List<String> validExpertEmails = getExpertEmailsForDomain(domain);
            List<CourseEnrichment> toDelete = new ArrayList<>();
            for (CourseEnrichment e : approved) {
                boolean contaminated = false;
                // Check if expert belongs to this domain
                if (e.getExpert() != null && e.getExpert().getUser() != null) {
                    String expertEmail = e.getExpert().getUser().getEmail();
                    if (!validExpertEmails.contains(expertEmail)) contaminated = true;
                }
                // Check for cross-domain certs
                if (!contaminated && e.getCertificates() != null) {
                    for (ProfessionalCertificate cert : e.getCertificates()) {
                        if (!validCertNames.contains(cert.getNameEn())) { contaminated = true; break; }
                    }
                }
                // Check for cross-domain tools
                if (!contaminated && e.getTools() != null) {
                    for (Tool tool : e.getTools()) {
                        if (!validToolNames.contains(tool.getNameEn())) { contaminated = true; break; }
                    }
                }
                if (contaminated) {
                    toDelete.add(e);
                }
            }
            if (!toDelete.isEmpty()) {
                enrichmentRepository.deleteAll(toDelete);
                purgedStale += toDelete.size();
            }
        }
        entityManager.flush();
        entityManager.clear();
        System.out.println("SEEDER [PURGE]: Removed " + purgedNonTech + " non-technical + " + purgedStale + " stale cross-domain enrichments.");
    }

    // ═════════════════════════════════════════════════════════════════════
    // DOMAIN CLASSIFICATION ENGINE
    // ═════════════════════════════════════════════════════════════════════
    private String classifyCourse(Course course) {
        String n = (course.getNameEn() + " " + course.getNameAr()).toLowerCase();
        if (n.contains("database") || n.contains("قواعد بيانات")) return "DATABASE";
        if (n.contains("secur") || n.contains("crypt") || n.contains("أمن") || n.contains("تشفير")) return "SECURITY";
        if (n.contains("artificial intelligence") || n.contains("ذكاء") || n.contains("image") || n.contains("صور")
            || n.contains("signal") || n.contains("إشارات") || n.contains("soft computing") || n.contains("simulation")
            || n.contains("modeling") || n.contains("نمذجة") || n.contains("محاكاة")) return "AI_DATA";
        if (n.contains("network") || n.contains("شبكات") || n.contains("cloud") || n.contains("سحاب")
            || n.contains("wireless") || n.contains("لاسلك") || n.contains("تراسل") || n.contains("internet")
            || n.contains("إنترنت") || n.contains("sensor") || n.contains("حسية") || n.contains("multimedia")
            || n.contains("وسائط") || n.contains("mobile") || n.contains("متنقل")) return "NETWORKS";
        if (n.contains("circuit") || n.contains("دوائر") || n.contains("logic design") || n.contains("منطقي")
            || n.contains("processor") || n.contains("معالج") || n.contains("architecture") || n.contains("عمارة")
            || n.contains("embedded") || n.contains("مضمن") || n.contains("vlsi") || n.contains("متكاملة")
            || n.contains("control") || n.contains("تحكم") || n.contains("robot") || n.contains("روبوت")
            || n.contains("automat") || n.contains("أتمتة") || n.contains("digital system") || n.contains("رقمية")
            || n.contains("reconfigur") || n.contains("متشكلة") || n.contains("storage") || n.contains("تخزين")
            || n.contains("high per") || n.contains("عالية")) return "HARDWARE";
        if (n.contains("software") || n.contains("برمجيات") || n.contains("program") || n.contains("برمجة")
            || n.contains("compiler") || n.contains("مترجم") || n.contains("operating") || n.contains("تشغيل")
            || n.contains("ethics") || n.contains("أخلاقيات") || n.contains("real time") || n.contains("حقيقي")
            || n.contains("data structures") || n.contains("خوارزميات") || n.contains("training") || n.contains("تدريب")
            || n.contains("graduation") || n.contains("تخرج") || n.contains("selected") || n.contains("مختارة")
            || n.contains("research") || n.contains("بحث") || n.contains("systems prog") || n.contains("إدارة النظم")
            || n.contains("project manage") || n.contains("إدارة مشروع")) return "SOFTWARE";
        if (n.contains("math") || n.contains("رياضيات") || n.contains("calculus") || n.contains("تفاضل")
            || n.contains("تكامل") || n.contains("algebra") || n.contains("جبر") || n.contains("statist")
            || n.contains("إحصاء") || n.contains("احتمال") || n.contains("physics") || n.contains("فيزياء")
            || n.contains("discrete") || n.contains("متقطعة") || n.contains("number theory") || n.contains("أعداد")
            || n.contains("differential eq") || n.contains("معادلات")) return "MATH";
        if (n.contains("computer") || n.contains("حاسب")) return "SOFTWARE";
        return "SOFTWARE";
    }

    // ── Domain → Skill names (must match nameEn in DB) ──
    private List<String> getSkillsForDomain(String domain) {
        switch (domain) {
            case "AI_DATA": return List.of("Machine Learning","Deep Learning","Natural Language Processing","Computer Vision","Data Mining","MLOps","LLM Fine-tuning","Retrieval-Augmented Generation","Prompt Engineering","Feature Engineering","Time Series Analysis","Reinforcement Learning","Generative AI","Transfer Learning","Data Lakehouse Architecture","ETL Pipeline Design","Statistical Modeling","Big Data Analytics","Edge AI","Federated Learning","Model Optimization","Data Governance","Anomaly Detection","Recommendation Systems","Speech Recognition","Sentiment Analysis","Knowledge Graphs","AutoML","Neural Architecture Search","Signal Processing","Data Structures");
            case "SECURITY": return List.of("Penetration Testing","Cryptography","Cybersecurity Fundamentals","Risk Management","Zero Trust Architecture","SOC Analysis","Malware Reverse Engineering","Incident Response","Threat Intelligence","Digital Forensics","Vulnerability Assessment","Security Architecture","Network Security","Application Security","Cloud Security","Identity & Access Management","SIEM Management","Red Team Operations","Blue Team Defense","Threat Hunting","Security Automation","Container Security","API Security","Security Governance","Linux Administration");
            case "SOFTWARE": return List.of("Object-Oriented Programming","Agile Methodology","RESTful API Design","Microservices Architecture","CI/CD Pipelines","Software Testing & QA","DevOps Practices","Frontend Development","Mobile Development","Project Management","Domain-Driven Design","GraphQL API Development","WebSocket Programming","Test-Driven Development","Clean Architecture","SOLID Principles","Design Patterns","Event-Driven Architecture","Serverless Architecture","API Gateway Design","Performance Optimization","System Design","Distributed Systems","Concurrency Programming","Infrastructure as Code","Site Reliability Engineering","Data Structures");
            case "NETWORKS": return List.of("Cloud Architecture","Linux Administration","Network Routing & Switching","SDN Architecture","Network Automation","Load Balancing","Network Monitoring","Cloud Migration","Multi-Cloud Strategy","Edge Computing","CDN Architecture","Network Virtualization","BGP Routing","Cybersecurity Fundamentals","Network Security");
            case "DATABASE": return List.of("Database Management","SQL Optimization","Data Structures","Database Design","Data Modeling","Query Performance Tuning","Data Warehousing","NoSQL Design Patterns","Database Migration","Replication & Sharding","Transaction Management","Schema Design","Database Monitoring","ETL Pipeline Design","Data Governance","Big Data Analytics");
            case "HARDWARE": return List.of("Embedded Systems Programming","Digital Circuit Design","Control Theory","Signal Processing","FPGA Design","PCB Design","Microcontroller Programming","RTOS Development","Hardware Description Languages","SoC Design","Sensor Integration","IoT Protocols","PLC Programming","Robotics Programming","ASIC Design");
            case "MATH": return List.of("Data Structures","Machine Learning","Deep Learning","Statistical Modeling","Numerical Methods","Optimization Theory","Mathematical Modeling","Graph Theory","Probability Theory","Operations Research","Stochastic Processes","Cryptographic Mathematics","Feature Engineering","Time Series Analysis");
            default: return List.of("Object-Oriented Programming","Data Structures");
        }
    }

    // ── Domain → Tool names (must match nameEn in DB) ──
    private List<String> getToolsForDomain(String domain) {
        switch (domain) {
            case "AI_DATA": return List.of("TensorFlow","PyTorch","Python","Hadoop","Power BI","MATLAB","Snowflake","Databricks","HuggingFace Transformers","MLflow","Apache Spark","Jupyter Notebook","Pandas","NumPy","Scikit-learn","Keras","Apache Airflow","LangChain","OpenCV","Tableau","Amazon SageMaker","Azure ML Studio","Google Colab","Weights & Biases","DVC");
            case "SECURITY": return List.of("Kali Linux","Wireshark","Metasploit","Burp Suite","Linux","CrowdStrike","Splunk","Fortinet FortiGate","Nmap","Snort","Nessus","OWASP ZAP","Qualys","Palo Alto Networks","HashiCorp Vault","Elastic SIEM","IBM QRadar","SentinelOne","Ghidra","OpenVAS","Suricata");
            case "SOFTWARE": return List.of("Spring Boot","React","Docker","Git","Jenkins","IntelliJ IDEA","Postman","Jira","Selenium","Flutter","Apache Kafka","Redis","Next.js","GraphQL","VS Code","GitHub Actions","SonarQube","Gradle","Maven","Nginx","RabbitMQ","Prometheus","Grafana","Swagger","ArgoCD","Kubernetes");
            case "NETWORKS": return List.of("Cisco Packet Tracer","Kubernetes","Terraform","AWS","Azure","Linux","GNS3","Nagios","Zabbix","Ansible","CloudFormation","Istio","Cloudflare","OpenStack","VMware vSphere","AWS CloudWatch","Envoy Proxy");
            case "DATABASE": return List.of("PostgreSQL","MySQL","Oracle Database","MongoDB","Power BI","Apache Cassandra","Neo4j","Amazon RDS","Elasticsearch","MariaDB","DynamoDB","InfluxDB","pgAdmin","DataGrip","Liquibase","Oracle RAC");
            case "HARDWARE": return List.of("Arduino","Xilinx Vivado","MATLAB","Simulink","Linux","Altium Designer","KiCad","STM32CubeIDE","Quartus Prime","LTSpice","LabVIEW","Proteus","PlatformIO","FreeRTOS");
            case "MATH": return List.of("Python","MATLAB","TensorFlow","Jupyter Notebook","NumPy","Pandas","Scikit-learn");
            default: return List.of("Git","Docker","VS Code");
        }
    }

    // ── Domain → Cert names (must match nameEn in DB) ──
    private List<String> getCertsForDomain(String domain) {
        switch (domain) {
            case "AI_DATA": return List.of("Microsoft Certified: Azure AI Engineer","Google Cloud Professional Data Engineer","AWS Certified Data Analytics","IBM Data Science Professional","AWS Machine Learning Specialty","Databricks Certified Data Engineer","TensorFlow Developer Certificate","Google Professional ML Engineer","Snowflake SnowPro Core","Azure Data Scientist Associate","Azure Data Engineer Associate","Cloudera Data Platform Generalist","SAS Certified Data Scientist","Deep Learning Specialization");
            case "SECURITY": return List.of("CISSP - Certified Information Systems Security Professional","CompTIA Security+","CEH - Certified Ethical Hacker","CISM - Certified Information Security Manager","OSCP - Offensive Security Certified Professional","GCIH - GIAC Incident Handler","CompTIA CASP+","GPEN - GIAC Penetration Tester","GSEC - GIAC Security Essentials","CCSP - Certified Cloud Security Professional","CySA+ - CompTIA Cybersecurity Analyst","CISA - Certified Information Systems Auditor","CHFI - Computer Hacking Forensic Investigator","CRISC - Certified in Risk and IS Control","ISO 27001 Lead Auditor");
            case "SOFTWARE": return List.of("AWS Certified Developer","PMP - Project Management Professional","Certified ScrumMaster (CSM)","ISTQB Certified Tester","Spring Certified Professional","AWS DevOps Engineer Professional","CKAD - Certified Kubernetes App Developer","PMI-ACP - Agile Certified Practitioner","Oracle Java SE Developer","Red Hat Certified Engineer","Azure DevOps Engineer Expert","GitHub Actions Certification","Docker Certified Associate","SAFe Agilist","Certified Kubernetes Administrator (CKA)");
            case "NETWORKS": return List.of("AWS Certified Solutions Architect","Cisco CCNA","Cisco CCNP","Certified Kubernetes Administrator (CKA)","Terraform Associate","Google Associate Cloud Engineer","AWS Advanced Networking Specialty","Azure Solutions Architect Expert","Azure Network Engineer Associate","CompTIA Network+","Juniper JNCIA","VMware VCP-DCV");
            case "DATABASE": return List.of("Oracle Database SQL Certified Associate","Oracle Certified Professional","MongoDB Developer Certification","AWS Database Specialty","Azure Database Administrator","PostgreSQL Certified Associate","Cassandra Administrator Certification","Redis Certified Developer","MariaDB Certified DBA","Neo4j Certified Professional");
            case "HARDWARE": return List.of("Cisco CCNA","CompTIA A+","ARM Accredited Engineer");
            case "MATH": return List.of("Google Cloud Professional Data Engineer","IBM Data Science Professional","SAS Certified Data Scientist","Deep Learning Specialization","TensorFlow Developer Certificate");
            default: return List.of("AWS Certified Developer");
        }
    }

    // ── Domain → Expert emails ──
    private List<String> getExpertEmailsForDomain(String domain) {
        switch (domain) {
            case "AI_DATA": return List.of("khalid.ai@sdaia.gov.sa","noura.data@aramco.com","yasser.bigdata@sabic.com","tariq.ai@tuwaiq.sa","layla.ml@google.com","mohammed.ds@microsoft.com","nada.nlp@sdaia.gov.sa","sultan.analytics@neom.com");
            case "SECURITY": return List.of("sara.sec@nca.gov.sa","mona.sec@sit.sa","ali.sec@cert.gov.sa","khadija.soc@stc.com.sa","abdulaziz.ir@aramco.com","tamara.grc@elm.sa","hassan.pen@nca.gov.sa","manal.forensics@cert.gov.sa");
            case "SOFTWARE": return List.of("ahmad.sw@stc.com.sa","reem.web@neom.com","turki.pm@lean.sa","hala.mobile@stcpay.com.sa","layan.qa@tahaluf.com","saleh.sw@thiqah.sa","fawaz.game@misk.org.sa","dalal.ui@sdaia.gov.sa","youssef.arch@amazon.com","ghada.devops@stc.com.sa","adel.backend@neom.com","rasha.fullstack@elm.sa","norah.mobile@sdaia.gov.sa","hamad.qa@thiqah.sa");
            case "NETWORKS": return List.of("faisal.cloud@elm.sa","hind.net@mobily.com.sa","saad.infra@wipro.com","nawal.cloud@aws.com","waleed.cloud@google.com","asma.sdn@stc.com.sa","sultan.noc@mobily.com.sa","ibrahim.aws@aws.com","haya.azure@microsoft.com");
            case "DATABASE": return List.of("bandar.db@alrajhibank.com","noura.data@aramco.com","fahad.dba@sabb.com","rakan.db@stc.com.sa","osama.bigdata@aramco.com","shaima.dba@snb.com","turki.dataeng@stc.com.sa","latifa.analytics@sabic.com");
            case "HARDWARE": return List.of("omar.sys@saip.gov.sa","majed.it@thales.com","nasser.hw@kacst.edu.sa","khaled.embedded@neom.com","aisha.iot@aramco.com","mansour.fpga@kacst.edu.sa","salma.robotics@sdaia.gov.sa");
            case "MATH": return List.of("khalid.ai@sdaia.gov.sa","tariq.ai@tuwaiq.sa","yasser.bigdata@sabic.com","layla.ml@google.com","mohammed.ds@microsoft.com");
            default: return List.of("ahmad.sw@stc.com.sa");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // THE MASSIVE ENRICHMENT SEEDER (DETERMINISTIC)
    // ═════════════════════════════════════════════════════════════════════
    private List<User> seedMassiveExperts() {
        String[][] expertData = {
            {"khalid.ai@sdaia.gov.sa", "خالد", "الغامدي", "AI Lead at SDAIA"},
            {"noura.data@aramco.com", "نورة", "القحطاني", "Data Architect at Aramco"},
            {"faisal.cloud@elm.sa", "فيصل", "العتيبي", "Cloud DevOps at Elm"},
            {"sara.sec@nca.gov.sa", "سارة", "الشهري", "CyberSec Manager at NCA"},
            {"ahmad.sw@stc.com.sa", "أحمد", "الدوسري", "SW Engineering Director at STC"},
            {"majed.it@thales.com", "ماجد", "المطيري", "IT Consultant at Thales"},
            {"yasser.bigdata@sabic.com", "ياسر", "العمري", "Big Data Engineer at SABIC"},
            {"reem.web@neom.com", "ريم", "السبيعي", "Frontend Tech Lead at NEOM"},
            {"turki.pm@lean.sa", "تركي", "الحربي", "Agile Project Manager at Lean Tech"},
            {"hala.mobile@stcpay.com.sa", "هالة", "الرويلي", "Mobile Dev Lead at stc pay"},
            {"omar.sys@saip.gov.sa", "عمر", "السالم", "Systems Analyst at SAIP"},
            {"layan.qa@tahaluf.com", "ليان", "العنزي", "QA Engineer at Tahaluf"},
            {"bandar.db@alrajhibank.com", "بندر", "الزهراني", "Database Admin at Al Rajhi Bank"},
            {"hind.net@mobily.com.sa", "هند", "الشمري", "Network Architect at Mobily"},
            {"saad.infra@wipro.com", "سعد", "التميمي", "Infrastructure Specialist at Wipro"},
            {"mona.sec@sit.sa", "منى", "اليامي", "InfoSec Analyst at SITE"},
            {"tariq.ai@tuwaiq.sa", "طارق", "الدعجاني", "ML Engineer at Tuwaiq Academy"},
            {"dalal.ui@sdaia.gov.sa", "دلال", "المحسن", "UX/UI Designer at SDAIA"},
            {"saleh.sw@thiqah.sa", "صالح", "الصالح", "Software Architect at Thiqah"},
            {"nawal.cloud@aws.com", "نوال", "السعدون", "Cloud Support Engineer at AWS"},
            {"fawaz.game@misk.org.sa", "فواز", "البلوي", "Game Developer at Misk Foundation"},
            // v1.21: Added experts to ensure ≥3 per domain
            {"fahad.dba@sabb.com", "فهد", "القرني", "Senior DBA at SABB Bank"},
            {"ali.sec@cert.gov.sa", "علي", "الحارثي", "SOC Team Lead at CERT SA"},
            {"nasser.hw@kacst.edu.sa", "ناصر", "العسيري", "Embedded Systems Engineer at KACST"},
            {"rakan.db@stc.com.sa", "راكان", "الشريف", "Data Engineer at STC"},
            // ── v1.24: AI/DATA Experts ──
            {"layla.ml@google.com", "ليلى", "الفهد", "Senior ML Engineer at Google"},
            {"mohammed.ds@microsoft.com", "محمد", "الخليفة", "Data Science Lead at Microsoft"},
            {"nada.nlp@sdaia.gov.sa", "ندى", "المنصور", "NLP Researcher at SDAIA"},
            {"sultan.analytics@neom.com", "سلطان", "البقمي", "Analytics Director at NEOM"},
            // ── v1.24: SECURITY Experts ──
            {"khadija.soc@stc.com.sa", "خديجة", "الهاجري", "SOC Manager at STC"},
            {"abdulaziz.ir@aramco.com", "عبدالعزيز", "العبدالله", "Incident Response Lead at Aramco"},
            {"tamara.grc@elm.sa", "تمارا", "الجهني", "GRC Specialist at Elm"},
            {"hassan.pen@nca.gov.sa", "حسن", "الردادي", "Senior Pen Tester at NCA"},
            {"manal.forensics@cert.gov.sa", "منال", "البشري", "Digital Forensics Lead at CERT SA"},
            // ── v1.24: SOFTWARE Experts ──
            {"youssef.arch@amazon.com", "يوسف", "المالكي", "Solutions Architect at Amazon"},
            {"ghada.devops@stc.com.sa", "غادة", "الأحمدي", "DevOps Lead at STC"},
            {"adel.backend@neom.com", "عادل", "العجمي", "Backend Engineering Manager at NEOM"},
            {"rasha.fullstack@elm.sa", "رشا", "العمري", "Full Stack Lead at Elm"},
            {"norah.mobile@sdaia.gov.sa", "نورة", "الثقفي", "Mobile Engineering Lead at SDAIA"},
            {"hamad.qa@thiqah.sa", "حمد", "المري", "QA Director at Thiqah"},
            // ── v1.24: NETWORKS Experts ──
            {"waleed.cloud@google.com", "وليد", "الرشيدي", "Cloud Infrastructure Lead at Google"},
            {"asma.sdn@stc.com.sa", "أسماء", "المطرفي", "SDN Architect at STC"},
            {"sultan.noc@mobily.com.sa", "سلطان", "اللحياني", "NOC Director at Mobily"},
            {"ibrahim.aws@aws.com", "إبراهيم", "الحمدان", "AWS Networking Specialist at AWS"},
            {"haya.azure@microsoft.com", "هيا", "السديري", "Azure Solutions Architect at Microsoft"},
            // ── v1.24: DATABASE Experts ──
            {"osama.bigdata@aramco.com", "أسامة", "الشهري", "Big Data Platform Engineer at Aramco"},
            {"shaima.dba@snb.com", "شيماء", "القرشي", "Senior DBA at SNB"},
            {"turki.dataeng@stc.com.sa", "تركي", "النفيعي", "Data Engineering Manager at STC"},
            {"latifa.analytics@sabic.com", "لطيفة", "الحربي", "Data Analytics Lead at SABIC"},
            // ── v1.24: HARDWARE Experts ──
            {"khaled.embedded@neom.com", "خالد", "الزمامي", "Embedded Systems Lead at NEOM"},
            {"aisha.iot@aramco.com", "عائشة", "العتيبي", "IoT Solutions Architect at Aramco"},
            {"mansour.fpga@kacst.edu.sa", "منصور", "الدوسري", "FPGA Design Engineer at KACST"},
            {"salma.robotics@sdaia.gov.sa", "سلمى", "البلوي", "Robotics Engineer at SDAIA"}
        };
        List<User> returnedUsers = new ArrayList<>();
        for (String[] data : expertData) {
            String email = data[0];
            User user = userRepository.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setUsername("expert_" + email.split("@")[0]);
                user.setPassword(passwordEncoder.encode("Expert@2026"));
                user.setFirstName(data[1]);
                user.setLastName(data[2]);
                user.setRole("ROLE_EXPERT");
                user.setStatus("APPROVED");
                user = userRepository.save(user);
            }
            returnedUsers.add(user);
            if (expertRepository.findByUserId(user.getId()).isEmpty()) {
                Expert expert = new Expert();
                expert.setUser(user);
                expert.setStatus(ExpertStatus.APPROVED);
                expert.setRating(4.5 + (Math.random() * 0.5));
                expert.setLinkedinUrl("https://linkedin.com/in/" + email.split("@")[0]);
                expert.setBioAr("خبير في " + data[3]);
                expert.setBioEn("Expert in " + data[3]);
                expertRepository.save(expert);
            }
        }
        System.out.println("SEEDER: " + expertData.length + " Experts seeded (incremental).");
        return returnedUsers;
    }

    private List<User> generateVoterPool() {
        List<User> voters = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            User u = userRepository.findByEmail("voter" + i + "@student.edu.sa");
            if (u == null) {
                u = new User(); u.setEmail("voter" + i + "@student.edu.sa");
                u.setUsername("voter_stu" + i); u.setPassword(passwordEncoder.encode("123456"));
                u.setFirstName("طالب"); u.setLastName(String.valueOf(i));
                u.setRole("ROLE_STUDENT"); u.setStatus("APPROVED");
                u = userRepository.save(u);
            }
            voters.add(u);
        }
        return voters;
    }

    private void seedMassiveEnrichments(List<User> experts) {
        List<Course> allCourses = courseRepository.findAll();
        List<Skill> allSkills = skillRepository.findAll();
        List<Tool> allTools = toolRepository.findAll();
        List<ProfessionalCertificate> allCerts = certRepository.findAll();
        List<User> voters = generateVoterPool();
        Random rand = new Random(42); // Deterministic seed
        int totalNew = 0, skipped = 0, skippedNonTech = 0;

        for (Course course : allCourses) {
            try {
                // ── RULE 1: Skip non-technical courses entirely ──
                if (isNonTechnicalCourse(course)) { skippedNonTech++; continue; }

                // ── RULE 2: Classify into domain ──
                String domain = classifyCourse(course);

                // ── Check existing enrichments ──
                int existing = enrichmentRepository
                    .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED).size();
                if (existing >= 5) { skipped++; continue; }

                int target = rand.nextInt(4) + 5; // 5 to 8
                int needed = target - existing;

                // ── RULE 3: Only domain-matched experts ──
                List<String> domainExpertEmails = getExpertEmailsForDomain(domain);
                List<User> domainExperts = new ArrayList<>();
                for (String email : domainExpertEmails) {
                    User u = userRepository.findByEmail(email);
                    if (u != null) domainExperts.add(u);
                }
                if (domainExperts.isEmpty()) continue;
                Collections.shuffle(domainExperts, rand);

                // ── RULE 4: Only domain-matched skills/tools/certs from DB ──
                List<String> domainSkillNames = getSkillsForDomain(domain);
                List<String> domainToolNames = getToolsForDomain(domain);
                List<String> domainCertNames = getCertsForDomain(domain);

                Set<Skill> domainSkills = new HashSet<>();
                for (Skill s : allSkills) { if (domainSkillNames.contains(s.getNameEn())) domainSkills.add(s); }
                Set<Tool> domainTools = new HashSet<>();
                for (Tool t : allTools) { if (domainToolNames.contains(t.getNameEn())) domainTools.add(t); }
                Set<ProfessionalCertificate> domainCerts = new HashSet<>();
                for (ProfessionalCertificate c : allCerts) { if (domainCertNames.contains(c.getNameEn())) domainCerts.add(c); }

                int injected = 0;
                int idx = 0;
                while (injected < needed) {
                    User expertUser = domainExperts.get(idx % domainExperts.size());
                    idx++;
                    Expert expert = expertRepository.findByUserId(expertUser.getId()).orElse(null);
                    if (expert == null) continue;

                    String content = generateDynamicContent(course, expertUser, domain);

                    CourseEnrichment enrichment = new CourseEnrichment();
                    enrichment.setExpert(expert);
                    enrichment.setCourse(course);
                    enrichment.setContent(content);
                    enrichment.setStatus(EnrichmentStatus.APPROVED);

                    // Assign 4-6 domain-matched skills
                    List<Skill> sList = new ArrayList<>(domainSkills);
                    Collections.shuffle(sList, rand);
                    Set<Skill> pickedSkills = new HashSet<>();
                    int ns = Math.min(rand.nextInt(3) + 4, sList.size());
                    for (int j = 0; j < ns; j++) pickedSkills.add(sList.get(j));
                    enrichment.setSkills(pickedSkills);

                    // Assign 4-6 domain-matched tools
                    List<Tool> tList = new ArrayList<>(domainTools);
                    Collections.shuffle(tList, rand);
                    Set<Tool> pickedTools = new HashSet<>();
                    int nt = Math.min(rand.nextInt(3) + 4, tList.size());
                    for (int j = 0; j < nt; j++) pickedTools.add(tList.get(j));
                    enrichment.setTools(pickedTools);

                    // Assign 1-3 domain-matched certs
                    List<ProfessionalCertificate> cList = new ArrayList<>(domainCerts);
                    Collections.shuffle(cList, rand);
                    Set<ProfessionalCertificate> pickedCerts = new HashSet<>();
                    int nc = Math.min(rand.nextInt(3) + 1, cList.size());
                    for (int j = 0; j < nc; j++) pickedCerts.add(cList.get(j));
                    enrichment.setCertificates(pickedCerts);

                    enrichment = enrichmentRepository.save(enrichment);

                    // Upvotes
                    int upvotes = rand.nextInt(41) + 10;
                    List<User> sv = new ArrayList<>(voters);
                    Collections.shuffle(sv, rand);
                    for (int k = 0; k < upvotes && k < sv.size(); k++) enrichment.addUpvote(sv.get(k));
                    enrichmentRepository.save(enrichment);
                    injected++;
                    totalNew++;
                }
            } catch (Exception e) {
                System.err.println("SEEDER [WARN]: " + course.getCode() + ": " + e.getMessage());
            }
        }
        System.out.println("SEEDER [DETERMINISTIC]: " + totalNew + " enrichments | " + skipped + " already >=3 | " + skippedNonTech + " non-technical skipped");
    }

    // ═════════════════════════════════════════════════════════════════════
    // PHASE 3: DYNAMIC ENTERPRISE MARKDOWN GENERATOR (v1.24)
    // ═════════════════════════════════════════════════════════════════════
    private String generateDynamicContent(Course course, User expert, String domain) {
        String courseAr = course.getNameAr();
        String courseEn = course.getNameEn();
        Optional<Expert> expOpt = expertRepository.findByUserId(expert.getId());
        String company = "مؤسسات تقنية رائدة";
        if (expOpt.isPresent() && expOpt.get().getBioEn() != null && expOpt.get().getBioEn().contains("at ")) {
            company = expOpt.get().getBioEn().substring(expOpt.get().getBioEn().indexOf("at ") + 3);
        }
        String expertName = expert.getFirstName() + " " + expert.getLastName();
        StringBuilder sb = new StringBuilder();
        sb.append("## 🎯 إثراء مهني: ").append(courseAr).append("\n\n");
        sb.append("> **الخبير:** ").append(expertName).append(" — **").append(company).append("**\n\n");
        sb.append("### 🏭 التطبيق العملي في بيئة العمل\n\n");
        switch (domain) {
            case "AI_DATA":
                sb.append("البيانات هي نفط العصر الرقمي. في **").append(company).append("**، نعتمد يومياً على مفاهيم مقرر **").append(courseAr)
                  .append("** لبناء نماذج التعلم العميق ومعالجة اللغات الطبيعية التي تدعم مشاريع الذكاء الاصطناعي ضمن رؤية 2030.")
                  .append(" نستخدم PyTorch وTensorFlow عبر Databricks وApache Spark، وMLflow لتتبع التجارب، وHuggingFace لنماذج LLM، وSnowflake لمستودعات البيانات.")
                  .append(" هذه التقنيات تُطبَّق في مشاريع **سدايا** للحكومة الذكية ومبادرات **نيوم** للمدن المعرفية.\n\n");
                break;
            case "SECURITY":
                sb.append("حماية الفضاء السيبراني السعودي أولوية وطنية. في **").append(company).append("**، نطبق مفاهيم **").append(courseAr)
                  .append("** لحماية البنية التحتية الحرجة من التهديدات المتقدمة المستمرة (APTs).")
                  .append(" نستخدم CrowdStrike وSentinelOne للكشف والاستجابة، وSplunk وElastic SIEM للمراقبة، وGhidra للهندسة العكسية.")
                  .append(" إطار عمل **NCA** يتطلب هذه الكفاءات في كل المؤسسات الحكومية والمالية.\n\n");
                break;
            case "SOFTWARE":
                sb.append("في **").append(company).append("**، نعتمد على أساسيات مقرر **").append(courseAr)
                  .append("** لبناء أنظمة مؤسسية قابلة للتطوير تخدم ملايين المستخدمين.")
                  .append(" نستخدم Spring Boot وNext.js للـ Full-Stack، وKafka وRabbitMQ للأنظمة الموجهة بالأحداث، وDocker وKubernetes وArgoCD لخطوط CI/CD.")
                  .append(" منظومة **STC** و**ثقة** و**نيوم** تعتمد على هذه البنية لتحقيق رؤية 2030.\n\n");
                break;
            case "NETWORKS":
                sb.append("البنية التحتية الشبكية عمود التحول الرقمي. في **").append(company).append("**، نطبق مفاهيم **").append(courseAr)
                  .append("** لبناء شبكات عالية التوفر بتقنيات SDN وNFV. نستخدم Terraform وAnsible للبنية التحتية ككود، وIstio لشبكات الخدمات،")
                  .append(" وAWS CloudWatch وZabbix للمراقبة. مشاريع **نيوم** و**Elm** تتطلب بنية شبكية متطورة.\n\n");
                break;
            case "DATABASE":
                sb.append("القرارات الاستراتيجية تُبنى على بيانات موثوقة. في **").append(company).append("**، نوظف مفاهيم **").append(courseAr)
                  .append("** لتصميم قواعد بيانات مؤسسية. نستخدم Oracle RAC وPostgreSQL للعلائقية، وCassandra وDynamoDB للـ NoSQL،")
                  .append(" وElasticsearch للبحث. **البنوك السعودية** كالراجحي وSNB ومراكز بيانات **أرامكو** تعتمد على هذه الأنظمة.\n\n");
                break;
            case "HARDWARE":
                sb.append("الأنظمة المضمنة والدوائر المتكاملة أساس IoT والروبوتات. في **").append(company).append("**، نطبق مفاهيم **").append(courseAr)
                  .append("** لتصميم أنظمة إلكترونية متقدمة. نستخدم STM32CubeIDE وFreeRTOS للأنظمة المضمنة، وQuartus Prime وXilinx Vivado لتصميم FPGA.")
                  .append(" مشاريع **نيوم** و**SAMI** و**كاكست** تتطلب هذه الخبرات.\n\n");
                break;
            case "MATH":
                sb.append("قد يبدو مقرر **").append(courseAr).append("** نظرياً، لكنه الأساس لكل التقنيات الحديثة. في **").append(company)
                  .append("**، نستخدم الجبر الخطي في التعلم العميق، والتفاضل في تحسين النماذج (Gradient Descent)، والإحصاء في تقييم النماذج.")
                  .append(" أدوات NumPy وPandas وScikit-learn تعتمد كلياً على هذه الأسس.\n\n");
                break;
            default:
                sb.append("مقرر **").append(courseAr).append("** ركيزة أساسية في بناء العقلية الهندسية في **").append(company).append("**.\n\n");
                break;
        }
        sb.append("### 🗺️ المسار المهني والشهادات\n\n");
        switch (domain) {
            case "AI_DATA":
                sb.append("| المرحلة | المسمى الوظيفي | الخبرة |\n|---|---|---|\n");
                sb.append("| Entry | محلل بيانات / ML Engineer مبتدئ | 0-2 سنوات |\n");
                sb.append("| Senior | مهندس تعلم آلة أول / MLOps | 3-6 سنوات |\n");
                sb.append("| Lead | قائد فريق AI / Principal DS | 7-10 سنوات |\n");
                sb.append("| C-Level | Chief Data Officer | 10+ سنوات |\n\n");
                break;
            case "SECURITY":
                sb.append("| المرحلة | المسمى الوظيفي | الخبرة |\n|---|---|---|\n");
                sb.append("| Entry | محلل SOC / Security Analyst | 0-2 سنوات |\n");
                sb.append("| Senior | Pen Tester أول / Threat Hunter | 3-6 سنوات |\n");
                sb.append("| Lead | Security Architect / Red Team Lead | 7-10 سنوات |\n");
                sb.append("| C-Level | CISO | 10+ سنوات |\n\n");
                break;
            case "SOFTWARE":
                sb.append("| المرحلة | المسمى الوظيفي | الخبرة |\n|---|---|---|\n");
                sb.append("| Entry | Junior Developer | 0-2 سنوات |\n");
                sb.append("| Senior | Senior / Staff Engineer | 3-6 سنوات |\n");
                sb.append("| Lead | Architect / Engineering Manager | 7-10 سنوات |\n");
                sb.append("| C-Level | VP of Engineering / CTO | 10+ سنوات |\n\n");
                break;
            default:
                sb.append("دمج المفاهيم الأكاديمية مع الأدوات الحديثة يُعد الطريق الأمثل لدخول سوق العمل.\n\n");
                break;
        }
        sb.append("### 🇸🇦 الربط برؤية 2030\n\n");
        sb.append("هذا المقرر يدعم أهداف رؤية 2030 في بناء اقتصاد معرفي وتأهيل كوادر وطنية متخصصة في **").append(courseEn).append("**.\n\n");
        sb.append("### 💡 نصيحة الخبير\n\n");
        sb.append("> *\"لا تتعاملوا مع مقرر **").append(courseAr).append("** على أنه مجرد متطلب أكاديمي. ");
        sb.append("استثمروا في فهم تطبيقاته وابنوا مشاريع حقيقية على GitHub، ");
        sb.append("فسوق العمل السعودي يبحث عن خريجين يمتلكون المعرفة التطبيقية.\"*\n\n");
        sb.append("— **").append(expertName).append("**، ").append(company);
        return sb.toString();
    }
}

