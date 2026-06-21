// src/main/resources/static/js/profile-panel.js
// Modern Profile Management Panel - Interactive Behavior

document.addEventListener('DOMContentLoaded', function() {
    const profilePanel = document.getElementById('profilePanelCard');
    const profileOverlay = document.getElementById('profilePanelOverlay');
    const profilePanelClose = document.getElementById('profilePanelClose');
    const profilePanelTrigger = document.getElementById('profilePanelTrigger');
    
    // Get all elements that should trigger the profile panel
    // This includes profile avatar in navbar and any element with data attribute
    const profileTriggers = document.querySelectorAll('[data-open-profile-panel]');
    
    // ==================================================
    // PROFILE PANEL OPEN/CLOSE FUNCTIONS
    // ==================================================
    
    /**
     * Opens the profile management panel with smooth animation
     */
    function openProfilePanel() {
        if (profilePanel && profileOverlay) {
            profilePanel.classList.add('active');
            profileOverlay.classList.add('active');
            document.body.style.overflow = 'hidden'; // Prevent background scroll
        }
    }
    
    /**
     * Closes the profile management panel with smooth animation
     */
    function closeProfilePanel() {
        if (profilePanel && profileOverlay) {
            profilePanel.classList.remove('active');
            profileOverlay.classList.remove('active');
            document.body.style.overflow = ''; // Restore scroll
        }
    }
    
    // ==================================================
    // EVENT LISTENERS FOR PROFILE PANEL
    // ==================================================
    
    // Close button
    if (profilePanelClose) {
        profilePanelClose.addEventListener('click', closeProfilePanel);
    }
    
    // Overlay click (close on outside click)
    if (profileOverlay) {
        profileOverlay.addEventListener('click', function(e) {
            if (e.target === this) {
                closeProfilePanel();
            }
        });
    }
    
    // Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && profilePanel?.classList.contains('active')) {
            closeProfilePanel();
        }
    });
    
    // Profile triggers (any element with data-open-profile-panel attribute)
    profileTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            openProfilePanel();
        });
    });
    
    // ==================================================
    // MODAL INTEGRATION
    // ==================================================
    
    // Close profile panel when edit/change password modals open
    const editProfileBtn = document.getElementById('editProfileBtnPanel');
    const changePasswordBtn = document.getElementById('changePasswordBtnPanel');
    
    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', function() {
            // Let Bootstrap handle modal opening
            // Profile panel will close due to data-bs-dismiss behavior
            setTimeout(closeProfilePanel, 100);
        });
    }
    
    if (changePasswordBtn) {
        changePasswordBtn.addEventListener('click', function() {
            setTimeout(closeProfilePanel, 100);
        });
    }
    
    // Close profile panel when modals are shown
    const editProfileModal = document.getElementById('editProfileModal');
    const changePasswordModal = document.getElementById('changePasswordModal');
    
    if (editProfileModal) {
        editProfileModal.addEventListener('show.bs.modal', closeProfilePanel);
    }
    
    if (changePasswordModal) {
        changePasswordModal.addEventListener('show.bs.modal', closeProfilePanel);
    }
    
    // ==================================================
    // PASSWORD VISIBILITY TOGGLE
    // ==================================================
    
    window.togglePasswordVisibility = function(event) {
        event.preventDefault();
        
        const button = event.currentTarget;
        const input = button.parentElement.querySelector('input');
        const icon = button.querySelector('i');
        
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
    };
    
    // ==================================================
    // MODAL CENTERING & POSITIONING
    // ==================================================
    
    function centerModal(modalElement) {
        const dialog = modalElement.querySelector('.modal-dialog');
        if (dialog) {
            const backdrop = modalElement.querySelector('.modal-backdrop');
            if (backdrop) {
                backdrop.style.zIndex = 1040;
            }
        }
    }
    
    // Center modals when they show
    const allModals = document.querySelectorAll('.modal');
    allModals.forEach(modal => {
        modal.addEventListener('shown.bs.modal', function() {
            centerModal(this);
        });
    });
    
    // ==================================================
    // FORM VALIDATION & SUCCESS STATES
    // ==================================================
    
    // Handle form submissions for visual feedback
    const profileForms = document.querySelectorAll('form[th\\:action*="/profile"]');
    
    profileForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Let the form submit normally for backend processing
            // The backend will handle redirects and success/error messages
            
            // Optional: Add loading state
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.textContent = 'Loading...';
                
                // Re-enable after a timeout (in case of errors)
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }, 3000);
            }
        });
    });
    
    // ==================================================
    // ACCESSIBILITY IMPROVEMENTS
    // ==================================================
    
    // Trap focus within modal when it's open
    function trapFocus(element) {
        const focusableElements = element.querySelectorAll(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        
        if (focusableElements.length === 0) return;
        
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];
        
        element.addEventListener('keydown', function(e) {
            if (e.key !== 'Tab') return;
            
            if (e.shiftKey) {
                if (document.activeElement === firstElement) {
                    e.preventDefault();
                    lastElement.focus();
                }
            } else {
                if (document.activeElement === lastElement) {
                    e.preventDefault();
                    firstElement.focus();
                }
            }
        });
    }
    
    // Apply focus trap to profile panel
    if (profilePanel) {
        trapFocus(profilePanel);
    }
    
    // ==================================================
    // RESPONSIVE ADJUSTMENTS
    // ==================================================
    
    function handleResponsive() {
        const windowWidth = window.innerWidth;
        
        // Adjust profile panel max-width on very small screens
        if (windowWidth < 400) {
            if (profilePanel) {
                profilePanel.style.maxWidth = '95vw';
            }
        }
    }
    
    // Check responsive on load and window resize
    handleResponsive();
    window.addEventListener('resize', handleResponsive);
    const currentUrl =
    window.location.pathname;

    const editField =
        document.getElementById('editReturnUrl');

    const passwordField =
        document.getElementById('passwordReturnUrl');

    if (editField) {
        editField.value = currentUrl;
    }

    if (passwordField) {
        passwordField.value = currentUrl;
    }

    const params = new URLSearchParams(window.location.search);

const toastEl = document.getElementById('toastMsg');

if (toastEl) {

    const toastBody = toastEl.querySelector('.toast-body');

    let message = null;

    if (params.has('profileUpdated')) {
        message = 'Profile Updated Successfully!!';
    }
    else if (params.has('profileUpdateFailed')) {
        message = 'Profile Update Failed ❌';
    }
    else if (params.has('passwordChanged')) {
        message = 'Password Changed Successfully 🔒';
    }
    else if (params.has('passwordFailed')) {
        message = 'Current Password Incorrect ❌';
    }
    else if (params.has('passwordMismatch')) {
        message = 'Passwords Do Not Match ⚠️';
    }

    if (message) {

        toastBody.innerText = message;

        const toast = new bootstrap.Toast(toastEl);

        toast.show();

        setTimeout(() => toast.hide(), 3000);

        history.replaceState(
            {},
            document.title,
            window.location.pathname
        );
    }
}
});

// ==================================================
// UTILITY: Open Profile Panel from Navbar
// ==================================================

/**
 * Function to attach profile panel opener to navbar avatar
 * Call this after navbar is loaded or from your navbar component
 */
function attachProfilePanelToNavbar() {
    // Look for navbar profile elements
    const navbarProfileAvatar = document.querySelector('.navbar-profile-avatar, [aria-label*="profile" i]');
    
    if (navbarProfileAvatar) {
        navbarProfileAvatar.addEventListener('click', function(e) {
            e.preventDefault();
            const profilePanel = document.getElementById('profilePanelCard');
            const profileOverlay = document.getElementById('profilePanelOverlay');
            
            if (profilePanel && profileOverlay) {
                profilePanel.classList.add('active');
                profileOverlay.classList.add('active');
                document.body.style.overflow = 'hidden';
            }
        });
        
        // Make it keyboard accessible
        navbarProfileAvatar.setAttribute('tabindex', '0');
        navbarProfileAvatar.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });
    }
}

// Auto-attach to navbar when document is ready
document.addEventListener('DOMContentLoaded', attachProfilePanelToNavbar);