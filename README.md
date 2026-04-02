# 🚀 منصة مقرري+ | Moqarari+ Enterprise Platform

![Version](https://img.shields.io/badge/Version-v2.3_Enterprise-blue.svg)
![Build](https://img.shields.io/badge/Build-SUCCESS-brightgreen.svg)
![Tests](https://img.shields.io/badge/Tests-43%2F43_PASSED-success.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-green.svg)
![Security](https://img.shields.io/badge/Security-Omni_Shield_Active-red.svg)

**مقرري+** هي منصة تعليمية ومهنية ثورية، لا تكتفي بتقديم المقررات الأكاديمية، بل تبني **"جسر المعرفة" (Knowledge Graph)** بين المناهج الجامعية وسوق العمل الحقيقي. تقوم المنصة باستخراج المهارات، والأدوات، والشهادات الاحترافية ديناميكياً لتوليد سير ذاتية للطلاب، وتبرز التأثير الحقيقي للخبراء المعتمدين من كبرى الشركات التقنية.

---

## 🏗️ المعمارية التقنية (The Tech Stack)
* **Backend:** Java 17, Spring Boot 3.2.4 (Web, Data JPA, Security, AOP, Validation).
* **Frontend:** Thymeleaf (Server-side rendering), Bootstrap 5, Chart.js, HTML5/CSS3/JS.
* **Database:** MySQL 8.0 (Live), Hibernate ORM.
* **Security:** Spring Security, BCrypt Password Hashing, RBAC (Role-Based Access Control).
* **Testing:** JUnit 5, MockMvc, DataJpaTest (Zero-Defect Protocol).
* **Architecture Patterns:** MVC, Aspect-Oriented Programming (AOP), Combinatorial Data Seeding.

---

## 📜 سجل التطور المعماري المعقد (The Architectural Journey)
يوثق هذا السجل التحول الجذري للمنصة من مشروع أولي إلى قلعة برمجية لا تقبل الاختراق.

### المرحلة الأولى: التطهير ومحرك البيانات (The Omni-Graph Engine)
* **v1.18 - الإدراج التراكمي الآمن (Additive Seeder):** التخلص من أقفال الحذف العشوائي (Bulldozer approach) واستبدالها بخوارزميات حقن آمنة (`findByCode`, `findByNameEn`) تمنع تكرار البيانات أو حذفها.
* **v1.19 - الخوارزمية الحتمية (The Deterministic Purge):** استئصال العشوائية. بناء قواميس بيانات ضخمة (AI, Security, Web, Databases) ومنع ربط المهارات التقنية بالمواد العامة (مثل الثقافة الإسلامية) بشكل قاطع.
* **v1.20 & v1.21 - بروتوكول انعدام الأخطاء (Zero-Defect Protocol):** بناء شبكة اختبارات آلية لمنع تداخل التخصصات (Domain Bleeding). تم برمجياً منع تعيين شهادة (CISSP) لمقرر (قواعد بيانات)، ومنع خبير من (سدايا) من شرح مادة (أنظمة تحكم/Hardware).
* **v1.22 - بروتوكول إبادة الأشباح (Zero-Ghost Protocol):** تعديل استعلامات قاعدة البيانات للواجهة الخارجية (`countByCourseIdAndStatus`) لضمان تطابق أرقام واجهة الكتالوج مع الإثراءات المعتمدة (Approved) فقط، ومسح أي تلوث بصري.
* **v1.24 - المحرك الأعظم (Omni-Graph Data Engine):** ضخ بيانات واقعية ضخمة (53 خبير من أرامكو، سدايا، نيوم، CERT)، و 66 شهادة عالمية، و 130+ أداة ومهارة. توليد أكثر من **386 إثراء تقني معقد** يحوي خرائط طريق، ونصائح، وربط بمبادرات رؤية 2030، كلها مولدة ديناميكياً ومحمية بالكامل.

### المرحلة الثانية: الحماية المطلقة (The Omni-Shield Architecture)
* **v1.23 - كيان الخزنة الحديدية (The Entity Iron Vault):** تفعيل (`@PrePersist` و `@PreUpdate`) في كيان `CourseEnrichment`. النظام الآن يرفض ذاتياً (Self-Healing) أي محاولة لحفظ إثراء تقني في مقرر غير تقني ويطلق استثناء `IllegalStateException` قبل الوصول لقاعدة البيانات.
* **إغلاق الثغرات الأمنية:** اكتشاف وإصلاح ثغرة خطيرة بإضافة `@EnableMethodSecurity` لضمان تفعيل `hasRole()` على جميع وحدات التحكم (Controllers) وعزل صلاحيات النظام.

### المرحلة الثالثة: المراقبة والتحكم (The Panopticon & God-Eye)
* **v2.1 - عين الصقر وسجل التدقيق (Spring AOP Audit Trail):** بناء نظام مراقبة صامت يعتمد على (Aspect-Oriented Programming). الكود الآن يراقب كل العمليات (حفظ، تعديل، حذف، موافقة، توثيق) عبر الـ Annotation المبتكر `@Auditable`، ويسجل (من الفاعل، وماذا فعل، ومتى) في جدول `audit_logs`.
* **v2.2 - واجهة القيادة العليا (Super Admin Unified Dashboard):** * إعادة تصميم لوحة السوبر آدمن بالكامل للتطابق مع الهوية البصرية البيضاء النظيفة للمنصة (Layout Unification).
   * دمج نظام الترجمة الديناميكي من قاعدة البيانات (`SiteContent`) لتعريب الشاشات 100% (RTL/LTR).
   * نظام التبويبات المدمج: لوحة المؤشرات (KPIs) مع رسوم Chart.js، سجل المراقبة الحي، ومصفوفة المستخدمين للتحكم الفوري (إيقاف حسابات، تغيير أدوار بتقنية AJAX).
   * استعادة وتأمين نظام "توليد المدراء" مع تمرير بيانات الدخول المؤقتة بذكاء عبر (Flash Attributes).

### المرحلة الرابعة: محرك الهويات المزدوجة (Dual-Faced Identity Engine)
* **v2.3 - الملفات الشخصية والبصمة التقنية:**
   * **الخزنة الخاصة (Private Vault):** لوحة تحكم آمنة للمستخدمين لتعديل نبذاتهم وروابطهم وتغيير كلمات المرور (مع تشفير BCrypt فوري)، محمية تماماً ضد ثغرات (IDOR).
   * **السيرة الذاتية التقنية (Student Tech CV):** رابط عام للطلاب (`/u/username`) يستخرج ديناميكياً المهارات والأدوات والشهادات من المقررات التي درسوها، محولاً إياها إلى لوحة شرف للتوظيف.
   * **واجهة تأثير الخبير (Expert Authority Page):** رابط عام للخبراء (`/expert/username`) يعرض الإثراءات المعتمدة، ونسبة تقييمهم، وابتكار **"مؤشر الأثر (Impact Score)"** الذي يحسب عدد الطلاب المستفيدين من خبراتهم.

---

## 🛡️ مصفوفة الأمان وصلاحيات الوصول (RBAC & Security Matrix)
تم تقسيم النظام إلى 4 مستويات من الصلاحيات المعزولة كلياً:
1. **STUDENT (الطالب):** تصفح، حفظ، تقييم، استعراض سيرته التقنية، وإدارة ملفه.
2. **EXPERT (الخبير):** إضافة إثراءات جديدة للمقررات، وإدارة ملفه العام.
3. **ADMIN (المدير):** توثيق حسابات الخبراء، الموافقة/الرفض على الإثراءات، وإدارة المقررات الدراسية.
4. **SUPER_ADMIN (القيادة العليا):** يمتلك كافة الصلاحيات السابقة، بالإضافة إلى مراقبة سجل التدقيق (Audit Logs)، إيقاف وتفعيل الحسابات، ترقية المستخدمين، وتوليد حسابات مدراء جديدة. (محمية بـ `@PreAuthorize("hasRole('SUPER_ADMIN')")`).

---

## 🧪 شبكة الاختبارات المعقدة (The Test Suite)
المنصة محصنة بشبكة اختبارات (Integration & Unit Tests) تتنفذ آلياً مع كل عملية بناء (Build):
* `DatabaseSeederVerificationTest`: يضمن عدم تداخل التخصصات (لا خبير أمن سيبراني في الذكاء الاصطناعي).
* `SystemIntegrityOmniTest`: يمسح قاعدة البيانات كاملة لضمان عدم وجود بيانات أشباح (Ghost Data).
* `OmniShieldControllerTest`: اختبارات `MockMvc` تؤكد أن صفحات السوبر آدمن محرمة على بقية المستخدمين.
* `IdentityEngineSecurityTest`: يضمن خصوصية البيانات (مستخدم (أ) لا يمكنه تعديل بيانات مستخدم (ب)).

---

## 🚀 دليل التشغيل السريع (Installation & Execution)

### المتطلبات الأساسية
* Java Development Kit (JDK) 17.
* Maven 3.8+.
* MySQL Server 8.0+.

### خطوات الإقلاع
1. **تجهيز قاعدة البيانات:**
   قم بإنشاء قاعدة بيانات فارغة في MySQL باسم `moqarariplus`.
2. **تكوين الإعدادات:**
   تأكد من تعديل بيانات الاتصال في `application.properties` (أو `application-dev.properties`):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/moqarariplus
   spring.datasource.username=root
   spring.datasource.password=your_password

البناء والاختبار (The Omni-Shield Verification):

Bash
mvn clean install
(ملاحظة: سيقوم هذا الأمر بتشغيل شبكة الاختبارات المعقدة لضمان سلامة الكود والبيانات قبل التثبيت).

التشغيل النهائي:

Bash
mvn spring-boot:run
(ملاحظة: عند التشغيل الأول، سيقوم محرك DatabaseSeeder بتوليد مئات الإثراءات والخبراء والمقررات والgit statusمهارات آلياً).

🔮 خارطة الطريق المستقبلية (Future Roadmap)
المرحلة القادمة (بوابة العقول - Mentorship Nexus): بناء نظام حجز مواعيد (1-on-1) للطلاب للحصول على استشارات تقنية مباشرة من الخبراء في المنصة، مدعوم بتقييمات حية وربط مع غرف افتراضية.

تم بناء هذه المنصة بصرامة عسكرية، وتفكير هندسي عميق، ورفض قاطع للأنصاف الحلول. 🏆