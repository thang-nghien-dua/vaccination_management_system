// Sidebar toggle functionality - Shared across all pages
function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn'); // Hamburger button (in sidebar header)
    const sidebarCloseBtn = document.getElementById('sidebarCloseBtn');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    const mainContent = document.querySelector('main');
    
    if (!sidebar) return;
    
    // Initialize: Sidebar luôn đóng trên mobile, mở trên desktop
    if (window.innerWidth < 1024) {
        sidebar.classList.add('-translate-x-full');
        if (sidebarOverlay) sidebarOverlay.classList.add('hidden');
    } else {
        sidebar.classList.remove('-translate-x-full');
        // Check if sidebar is collapsed (stored in localStorage)
        const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
        if (isCollapsed) {
            collapseSidebar();
        } else {
            expandSidebar();
        }
    }
    
    // Hamburger button: Toggle sidebar (mobile: open/close overlay, desktop: collapse/expand)
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevent event bubbling
            if (window.innerWidth < 1024) {
                // Mobile: Toggle overlay sidebar
                if (sidebar.classList.contains('-translate-x-full')) {
                    // Open sidebar
                    sidebar.classList.remove('-translate-x-full');
                    if (sidebarOverlay) sidebarOverlay.classList.remove('hidden');
                } else {
                    // Close sidebar
                    sidebar.classList.add('-translate-x-full');
                    if (sidebarOverlay) sidebarOverlay.classList.add('hidden');
                }
            } else {
                // Desktop: Toggle collapse/expand
                const isCollapsed = sidebar.classList.contains('lg:w-20');
                if (isCollapsed) {
                    expandSidebar();
                } else {
                    collapseSidebar();
                }
            }
        });
    }
    
    // Close sidebar (mobile)
    const closeSidebar = () => {
        sidebar.classList.add('-translate-x-full');
        if (sidebarOverlay) sidebarOverlay.classList.add('hidden');
    };
    
    if (sidebarCloseBtn) {
        sidebarCloseBtn.addEventListener('click', closeSidebar);
    }
    
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', closeSidebar);
    }
    
    // Collapse sidebar (desktop)
    function collapseSidebar() {
        sidebar.classList.add('lg:w-20');
        sidebar.classList.remove('lg:w-72');
        localStorage.setItem('sidebarCollapsed', 'true');
        
        // Hide text elements
        const textElements = sidebar.querySelectorAll('[id^="navText"], [id^="sidebar"]');
        textElements.forEach(el => {
            if (el.id && el.id.startsWith('navText')) {
                el.classList.add('lg:hidden');
            }
            if (el.id === 'sidebarLogoText' || el.id === 'sidebarUserName' || el.id === 'sidebarUserRole') {
                el.classList.add('lg:hidden');
            }
        });
        
        // Hide user profile section when collapsed
        const userProfileSection = document.getElementById('sidebarUserProfile');
        if (userProfileSection) {
            userProfileSection.classList.add('lg:hidden');
        }
        
        // Adjust main content margin
        if (mainContent) {
            mainContent.classList.remove('lg:ml-72');
            mainContent.classList.add('lg:ml-20');
        }
    }
    
    // Expand sidebar (desktop)
    function expandSidebar() {
        sidebar.classList.remove('lg:w-20');
        sidebar.classList.add('lg:w-72');
        localStorage.setItem('sidebarCollapsed', 'false');
        
        // Show text elements
        const textElements = sidebar.querySelectorAll('[id^="navText"], [id^="sidebar"]');
        textElements.forEach(el => {
            if (el.id && el.id.startsWith('navText')) {
                el.classList.remove('lg:hidden');
            }
            if (el.id === 'sidebarLogoText' || el.id === 'sidebarUserName' || el.id === 'sidebarUserRole') {
                el.classList.remove('lg:hidden');
            }
        });
        
        // Show user profile section when expanded
        const userProfileSection = document.getElementById('sidebarUserProfile');
        if (userProfileSection) {
            userProfileSection.classList.remove('lg:hidden');
        }
        
        // Hamburger button stays the same (no icon change needed)
        
        // Adjust main content margin
        if (mainContent) {
            mainContent.classList.remove('lg:ml-20');
            mainContent.classList.add('lg:ml-72');
        }
    }
    
    // Close sidebar khi click outside trên mobile
    document.addEventListener('click', (e) => {
        if (window.innerWidth < 1024) {
            if (sidebar && !sidebar.contains(e.target) && 
                sidebarToggleBtn && !sidebarToggleBtn.contains(e.target)) {
                if (!sidebar.classList.contains('-translate-x-full')) {
                    closeSidebar();
                }
            }
        }
    });
    
    // Handle resize
    window.addEventListener('resize', () => {
        if (window.innerWidth >= 1024) {
            // Desktop: Remove mobile hidden state
            sidebar.classList.remove('-translate-x-full');
            if (sidebarOverlay) sidebarOverlay.classList.add('hidden');
            // Restore collapsed state if needed
            const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
            if (isCollapsed) {
                collapseSidebar();
            } else {
                expandSidebar();
            }
        } else {
            // Mobile: Always close sidebar
            sidebar.classList.add('-translate-x-full');
            if (sidebarOverlay) sidebarOverlay.classList.add('hidden');
            // Reset to full width on mobile
            sidebar.classList.remove('lg:w-20');
            sidebar.classList.add('lg:w-72');
            // Show all text elements on mobile
            const textElements = sidebar.querySelectorAll('[id^="navText"], [id^="sidebar"]');
            textElements.forEach(el => {
                if (el.id && el.id.startsWith('navText')) {
                    el.classList.remove('lg:hidden');
                }
                if (el.id === 'sidebarLogoText' || el.id === 'sidebarUserName' || el.id === 'sidebarUserRole') {
                    el.classList.remove('lg:hidden');
                }
            });
            
            // Show user profile section on mobile
            const userProfileSection = document.getElementById('sidebarUserProfile');
            if (userProfileSection) {
                userProfileSection.classList.remove('lg:hidden');
            }
        }
    });
}

// Initialize sidebar when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setupSidebar);
} else {
    setupSidebar();
}

