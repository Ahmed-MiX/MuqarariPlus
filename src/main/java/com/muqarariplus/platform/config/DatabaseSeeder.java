package com.muqarariplus.platform.config;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.*;
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

    public DatabaseSeeder(UserRepository userRepository, CourseRepository courseRepository,
                          SiteContentRepository siteContentRepository, ExpertRepository expertRepository,
                          SkillRepository skillRepository, ToolRepository toolRepository,
                          ProfessionalCertificateRepository certRepository,
                          CourseEnrichmentRepository enrichmentRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.siteContentRepository = siteContentRepository;
        this.expertRepository = expertRepository;
        this.skillRepository = skillRepository;
        this.toolRepository = toolRepository;
        this.certRepository = certRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.passwordEncoder = passwordEncoder;
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
        seedMassiveEnrichments();
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
        if (courseRepository.count() > 0) return;
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
        for (String[] c : courses) {
            Course co = new Course(); co.setCode(c[0]); co.setNameEn(c[1]); co.setNameAr(c[2]);
            co.setDescriptionEn("CCIS standard course."); co.setDescriptionAr("مقرر ضمن كلية علوم الحاسب والمعلومات.");
            co.setUniversity("CCIS"); courseRepository.save(co);
        }
        System.out.println("SEEDER: 73 courses seeded.");
    }

    // ── SITE CONTENT (kept via helper to save space) ─────────────────────
    private void seedSiteContent() {
        if (siteContentRepository.count() > 0) return;
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
        System.out.println("SEEDER: SiteContent seeded.");
    }
    private void sc(String key, String en, String ar) {
        SiteContent s = new SiteContent(); s.setContentKey(key); s.setValueEn(en); s.setValueAr(ar); siteContentRepository.save(s);
    }

    private void seedIndustryHierarchy() {
        if (skillRepository.count() == 0) {
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
                {"Frontend Development","تطوير الواجهات الأمامية"},{"Cryptography","التشفير"}
            };
            for (String[] s : skills) { Skill sk = new Skill(); sk.setNameEn(s[0]); sk.setNameAr(s[1]); skillRepository.save(sk); }
            System.out.println("SEEDER: 20 Skills seeded.");
        }
        if (toolRepository.count() == 0) {
            String[][] tools = {
                {"Docker","دوكر"},{"Git","جِت"},{"Spring Boot","سبرنج بوت"},{"React","رياكت"},
                {"Kubernetes","كوبرنيتيز"},{"PostgreSQL","بوستقريسكيول"},{"Jenkins","جنكنز"},
                {"IntelliJ IDEA","إنتلي جي آيديا"},{"Postman","بوستمان"},{"AWS","أمازون ويب سيرفسز"},
                {"TensorFlow","تنسرفلو"},{"Python","بايثون"},{"Flutter","فلاتر"},{"Jira","جيرا"},
                {"MySQL","ماي إس كيو إل"},{"Linux","لينكس"},{"Wireshark","وايرشارك"},
                {"Azure","مايكروسوفت أزور"},{"Figma","فيجما"},{"Selenium","سيلينيوم"}
            };
            for (String[] t : tools) { Tool to = new Tool(); to.setNameEn(t[0]); to.setNameAr(t[1]); toolRepository.save(to); }
            System.out.println("SEEDER: 20 Tools seeded.");
        }
    }

    private void seedCertifications() {
        if (certRepository.count() > 0) return;
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
            {"Meta Certified Digital Marketing Associate","تسويق رقمي ميتا المعتمد","Meta"}
        };
        for (String[] c : certs) {
            ProfessionalCertificate pc = new ProfessionalCertificate(); pc.setNameEn(c[0]); pc.setNameAr(c[1]); pc.setIssuingBody(c[2]);
            certRepository.save(pc);
        }
        System.out.println("SEEDER: 15 Professional Certifications seeded.");
    }

    // ═════════════════════════════════════════════════════════════════════
    // THE MASSIVE ENRICHMENT SEEDER
    // ═════════════════════════════════════════════════════════════════════
    private void seedMassiveEnrichments() {
        if (enrichmentRepository.count() > 0) return;

        // ── Expert 1: Software Engineering ──────────────────────────────
        createExpertAndEnrichment(
            "fahad.se@elm.sa", "فهد", "العتيبي", "SE 2111",
            "## التطبيقات العملية في هندسة البرمجيات\n\n"
            + "في شركة **Elm** نطبق مبادئ هندسة البرمجيات يومياً في بناء الأنظمة الحكومية الرقمية. "
            + "نستخدم منهجية **Agile/Scrum** لتقسيم المشاريع الكبيرة إلى Sprints مدتها أسبوعان. "
            + "نعتمد بنية **Microservices** لضمان أن كل خدمة تُطوَّر وتُنشر بشكل مستقل عبر Docker و Kubernetes.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ المسار من **Junior Developer** ثم **Software Engineer** ثم **Senior Architect**. "
            + "الطلب في السوق السعودي مرتفع جداً مع رؤية 2030. شركات مثل Elm وNeom وSTC تبحث عن مهندسين يفهمون Design Patterns.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: Clean Architecture by Robert C. Martin\n"
            + "- دورة: Software Design and Architecture Specialization (Coursera)\n"
            + "- شهادة: AWS Certified Solutions Architect",
            new String[]{"Agile Methodology","Microservices Architecture","Software Testing & QA","RESTful API Design"},
            new String[]{"Docker","Git","Jenkins","Jira"},
            new String[]{"Certified ScrumMaster (CSM)","AWS Certified Solutions Architect"}
        );

        // ── Expert 2: Artificial Intelligence ───────────────────────────
        createExpertAndEnrichment(
            "noura.ai@sdaia.gov.sa", "نورة", "القحطاني", "CS 3501",
            "## التطبيقات العملية في الذكاء الاصطناعي\n\n"
            + "في **SDAIA** (هيئة البيانات والذكاء الاصطناعي) نعمل على مشاريع تحليل البيانات الضخمة لدعم صنع القرار الحكومي. "
            + "نستخدم **TensorFlow** و **PyTorch** لبناء نماذج Deep Learning لمعالجة اللغة العربية الطبيعية (NLP) وتحليل الصور الطبية.\n\n"
            + "## المسار المهني\n\n"
            + "المسار يبدأ من **Data Analyst** ثم **ML Engineer** ثم **AI Research Scientist**. "
            + "السعودية تستثمر مليارات في الذكاء الاصطناعي. مشاريع مثل منصة توكلنا وتحليل بيانات الحج تعتمد على هذه التقنيات.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: Deep Learning by Ian Goodfellow\n"
            + "- دورة: Deep Learning Specialization (Andrew Ng - Coursera)\n"
            + "- شهادة: Microsoft Certified Azure AI Engineer",
            new String[]{"Machine Learning","Deep Learning","Data Structures","Cloud Architecture"},
            new String[]{"TensorFlow","Python","AWS","Azure"},
            new String[]{"Microsoft Certified: Azure AI Engineer"}
        );

        // ── Expert 3: Database Management Systems ───────────────────────
        createExpertAndEnrichment(
            "ahmed.db@aramco.sa", "أحمد", "الدوسري", "IS 2511",
            "## التطبيقات العملية في قواعد البيانات\n\n"
            + "في **أرامكو** ندير أكبر قواعد بيانات صناعية في العالم. نستخدم **Oracle Database** لإدارة بيانات الإنتاج والاستكشاف، "
            + "و**PostgreSQL** للأنظمة التحليلية. تحسين استعلامات SQL (Query Optimization) يوفر ملايين الريالات سنوياً.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ من **Database Administrator** ثم **Data Engineer** ثم **Chief Data Officer**. "
            + "الشركات النفطية والبنوك السعودية تحتاج خبراء قواعد بيانات بشكل مستمر.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: Database System Concepts by Silberschatz\n"
            + "- دورة: Oracle Database SQL Certified Associate\n"
            + "- أداة: تعلم استخدام pgAdmin و Oracle SQL Developer",
            new String[]{"Database Management","Data Structures","Cloud Architecture","RESTful API Design"},
            new String[]{"PostgreSQL","MySQL","AWS","Docker"},
            new String[]{"Oracle Database SQL Certified Associate","AWS Certified Solutions Architect"}
        );

        // ── Expert 4: Information Security ──────────────────────────────
        createExpertAndEnrichment(
            "sara.sec@nca.gov.sa", "سارة", "الشهري", "CE 4711",
            "## التطبيقات العملية في أمن المعلومات\n\n"
            + "في **الهيئة الوطنية للأمن السيبراني (NCA)** نحمي البنية التحتية الرقمية للمملكة. "
            + "نجري اختبارات اختراق (Penetration Testing) دورية باستخدام أدوات مثل **Wireshark** و **Burp Suite**. "
            + "نطبق معايير التشفير المتقدمة وبروتوكولات Zero Trust Architecture.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ من **SOC Analyst** ثم **Penetration Tester** ثم **CISO**. "
            + "الأمن السيبراني من أعلى المجالات أجراً في السعودية مع تزايد التهديدات الإلكترونية.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: The Web Application Hacker's Handbook\n"
            + "- دورة: CompTIA Security+ Certification\n"
            + "- شهادة: CISSP من ISC2",
            new String[]{"Cybersecurity Fundamentals","Penetration Testing","Cryptography","Linux Administration"},
            new String[]{"Wireshark","Linux","Git","Python"},
            new String[]{"CISSP - Certified Information Systems Security Professional","CompTIA Security+"}
        );

        // ── Expert 5: Mobile App Development ────────────────────────────
        createExpertAndEnrichment(
            "mohammed.mob@stc.com.sa", "محمد", "الحربي", "CS 2301",
            "## التطبيقات العملية في تطوير التطبيقات\n\n"
            + "في **STC** نطور تطبيقات الجوال التي يستخدمها ملايين العملاء يومياً. "
            + "نستخدم **Flutter** لبناء تطبيقات cross-platform بكفاءة عالية. "
            + "نطبق CI/CD باستخدام **Jenkins** لنشر التحديثات بشكل آلي وآمن كل أسبوع.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ من **Junior Mobile Developer** ثم **Senior Developer** ثم **Mobile Tech Lead**. "
            + "تطبيقات مثل MySTC و stc pay تحتاج مطورين محترفين في Dart و Swift.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- دورة: Flutter & Dart - The Complete Guide (Udemy)\n"
            + "- أداة: تعلم Android Studio و Xcode\n"
            + "- كتاب: Flutter in Action (Manning)",
            new String[]{"Mobile Development","CI/CD Pipelines","Object-Oriented Programming","Software Testing & QA"},
            new String[]{"Flutter","Git","Jenkins","Postman"},
            new String[]{"AWS Certified Developer"}
        );

        // ── Expert 6: Cloud DevOps ──────────────────────────────────────
        createExpertAndEnrichment(
            "khalid.devops@lean.sa", "خالد", "المالكي", "CS 3701",
            "## التطبيقات العملية في نظم التشغيل والسحابة\n\n"
            + "في **Lean Technologies** نبني بنية تحتية سحابية تخدم منصات التقنية المالية (FinTech). "
            + "نستخدم **Docker** لتحزيم التطبيقات و**Kubernetes** لتنسيق الحاويات على نطاق واسع. "
            + "فهم نظم التشغيل وإدارة العمليات (Process Management) أساسي لتحسين أداء الخوادم.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ من **System Administrator** ثم **DevOps Engineer** ثم **Cloud Architect**. "
            + "شهادة CKA و AWS Solutions Architect مطلوبة بشدة في السوق السعودي.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: The Phoenix Project (Gene Kim)\n"
            + "- دورة: CKA - Certified Kubernetes Administrator\n"
            + "- أداة: تعلم Terraform و Ansible",
            new String[]{"DevOps Practices","Cloud Architecture","Linux Administration","CI/CD Pipelines"},
            new String[]{"Docker","Kubernetes","Linux","AWS"},
            new String[]{"Certified Kubernetes Administrator (CKA)","AWS Certified Solutions Architect"}
        );

        // ── Expert 7: Full Stack Web Technologies ───────────────────────
        createExpertAndEnrichment(
            "reem.web@outlook.sa", "ريم", "السبيعي", "CS 2321",
            "## التطبيقات العملية في تقنيات الويب\n\n"
            + "كـ **Full Stack Tech Lead** في تطوير منصات الويب الحكومية، نعتمد على **React** للواجهات الأمامية "
            + "و**Spring Boot** للخدمات الخلفية مع **RESTful APIs**. فهم هياكل البيانات والخوارزميات ضروري لتحسين أداء قواعد البيانات والبحث.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ من **Frontend Developer** ثم **Full Stack Developer** ثم **Technical Lead**. "
            + "الطلب على مطوري Full Stack في السعودية يتزايد مع التحول الرقمي.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: Grokking Algorithms (Aditya Bhargava)\n"
            + "- دورة: Full Stack Open (University of Helsinki)\n"
            + "- أداة: تعلم TypeScript و Next.js",
            new String[]{"Frontend Development","RESTful API Design","Data Structures","Object-Oriented Programming"},
            new String[]{"React","Spring Boot","PostgreSQL","Git"},
            new String[]{"AWS Certified Developer"}
        );

        // ── Expert 8: IT Project Management ─────────────────────────────
        createExpertAndEnrichment(
            "turki.pm@outlook.sa", "تركي", "الغامدي", "SE 4231",
            "## التطبيقات العملية في إدارة المشاريع\n\n"
            + "كـ **IT Project Manager** أدرت مشاريع تحول رقمي بملايين الريالات. "
            + "نستخدم **Jira** لتتبع المهام و**Scrum Framework** لتنظيم الفرق. "
            + "إدارة المخاطر (Risk Management) وتحليل أصحاب المصلحة (Stakeholder Analysis) من أهم المهارات العملية.\n\n"
            + "## المسار المهني\n\n"
            + "يبدأ من **Scrum Master** ثم **Project Manager** ثم **PMO Director**. "
            + "شهادة PMP من PMI تزيد الراتب بنسبة 20-30% في السوق السعودي.\n\n"
            + "## مصادر إثرائية\n\n"
            + "- كتاب: PMBOK Guide 7th Edition\n"
            + "- دورة: PMP Certification Training (PMI)\n"
            + "- أداة: تعلم MS Project و Confluence",
            new String[]{"Project Management","Risk Management","Agile Methodology","Software Testing & QA"},
            new String[]{"Jira","Git","Postman","Figma"},
            new String[]{"PMP - Project Management Professional","Certified ScrumMaster (CSM)"}
        );

        System.out.println("SEEDER: 8 massive enrichments with experts, skills, tools, and certifications seeded.");
    }

    /**
     * Helper: Creates a User + Expert + CourseEnrichment in one shot.
     */
    private void createExpertAndEnrichment(String email, String firstName, String lastName,
                                           String courseCode, String content,
                                           String[] skillNames, String[] toolNames, String[] certNames) {
        // 1. Create or find the User
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("Expert@2026"));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole("ROLE_EXPERT");
            user.setStatus("APPROVED");
            user = userRepository.save(user);
        }

        // 2. Create Expert entity (APPROVED)
        Expert expert;
        Optional<Expert> existingExpert = expertRepository.findByUserId(user.getId());
        if (existingExpert.isPresent()) {
            expert = existingExpert.get();
        } else {
            expert = new Expert();
            expert.setUser(user);
            expert.setStatus(ExpertStatus.APPROVED);
            expert.setRating(4.5);
            expert.setLinkedinUrl("https://linkedin.com/in/" + email.split("@")[0]);
            expert = expertRepository.save(expert);
        }

        // 3. Find the Course
        List<Course> matchingCourses = courseRepository.findAll().stream()
                .filter(c -> c.getCode().equals(courseCode)).toList();
        if (matchingCourses.isEmpty()) return;
        Course course = matchingCourses.get(0);

        // 4. Resolve Skills
        Set<Skill> skills = new HashSet<>();
        for (String sn : skillNames) {
            skillRepository.findAll().stream().filter(s -> s.getNameEn().equals(sn)).findFirst().ifPresent(skills::add);
        }

        // 5. Resolve Tools
        Set<Tool> tools = new HashSet<>();
        for (String tn : toolNames) {
            toolRepository.findAll().stream().filter(t -> t.getNameEn().equals(tn)).findFirst().ifPresent(tools::add);
        }

        // 6. Resolve Certificates
        Set<ProfessionalCertificate> certs = new HashSet<>();
        for (String cn : certNames) {
            certRepository.findByNameEn(cn).ifPresent(certs::add);
        }

        // 7. Build and save enrichment
        CourseEnrichment enrichment = new CourseEnrichment();
        enrichment.setExpert(expert);
        enrichment.setCourse(course);
        enrichment.setContent(content);
        enrichment.setSkills(skills);
        enrichment.setTools(tools);
        enrichment.setCertificates(certs);
        enrichment.setStatus(EnrichmentStatus.APPROVED);
        enrichmentRepository.save(enrichment);
    }
}
