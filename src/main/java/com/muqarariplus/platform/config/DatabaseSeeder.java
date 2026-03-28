package com.muqarariplus.platform.config;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, CourseRepository courseRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedCourses();
    }

    private void seedUsers() {
        // SUPER ADMIN
        if (userRepository.findByUsername("admin") == null) {
            User superAdmin = new User();
            superAdmin.setUsername("admin");
            superAdmin.setEmail("admin@super.muqarariplus.com"); // Backend only
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
            admin.setEmail("sultan@muqarariplus.com"); // Backend only
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
            expert.setStatus("APPROVED"); // Forced approved for testing as requested
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
}
