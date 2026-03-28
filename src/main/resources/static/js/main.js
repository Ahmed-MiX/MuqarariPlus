document.addEventListener("DOMContentLoaded", () => {
    // Determine language from HTML tag (rendered by Thymeleaf)
    const currentLang = document.documentElement.lang || 'ar';
    const isRtl = currentLang === 'ar';
    
    // Accordion Logic
    const accordions = document.querySelectorAll('.accordion');
    
    accordions.forEach(acc => {
        const header = acc.querySelector('.accordion-header');
        header.addEventListener('click', () => {
            // Close all others
            accordions.forEach(other => {
                if (other !== acc) other.classList.remove('active');
            });
            // Toggle current
            acc.classList.toggle('active');
        });
    });

    // Simple Lang Switcher logic
    const langBtn = document.getElementById('langSwitcher');
    if (langBtn) {
        langBtn.addEventListener('click', () => {
            const url = new URL(window.location.href);
            // Toggle lang query param
            const param = url.searchParams.get('lang');
            const targetLang = (currentLang === 'ar') ? 'en' : 'ar';
            
            url.searchParams.set('lang', targetLang);
            window.location.href = url.toString();
        });
    }
});
