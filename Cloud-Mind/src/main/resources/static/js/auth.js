document.addEventListener('DOMContentLoaded', function() {
    // Password visibility toggle for signup
    const toggleSignupPassword = document.getElementById('toggleSignupPassword');
    const signupPassword = document.getElementById('signupPassword');
    const toggleSignupIcon = document.getElementById('toggleSignupIcon');

    if (toggleSignupPassword && signupPassword && toggleSignupIcon) {
        toggleSignupPassword.addEventListener('click', function() {
            const type = signupPassword.getAttribute('type') === 'password' ? 'text' : 'password';
            signupPassword.setAttribute('type', type);
            toggleSignupIcon.classList.toggle('bi-eye-slash', type === 'text');
            toggleSignupIcon.classList.toggle('bi-eye', type === 'password');
        });
    }

// Password visibility toggle for login
// Password visibility toggle for login
    const togglePassword = document.getElementById('togglePassword');
    const password = document.getElementById('password');
    const toggleIcon = document.getElementById('toggleIcon');

    if (togglePassword && password && toggleIcon) {
        togglePassword.addEventListener('click', function() {
            const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
            password.setAttribute('type', type);
            toggleIcon.classList.toggle('bi-eye-slash', type === 'text');
            toggleIcon.classList.toggle('bi-eye', type === 'password');
        });
    }

    // Password strength indicator
    const passwordInput = document.getElementById('signupPassword');
    const strengthText = document.getElementById('strengthText');
    const progressBar = document.querySelector('.password-strength .progress-bar');
    const strengthContainer = document.querySelector('.password-strength');

    if (passwordInput && strengthText && progressBar && strengthContainer) {
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            let score = 0;
            if (password.length >= 8) score++;
            if (password.length >= 12) score++;
            if (/[a-z]/.test(password)) score++;
            if (/[A-Z]/.test(password)) score++;
            if (/[0-9]/.test(password)) score++;
            if (/[^A-Za-z0-9]/.test(password)) score++;
            const level = Math.min(Math.floor(score / 1.5), 4);

            // Remove existing classes
            strengthContainer.classList.remove('password-weak', 'password-medium', 'password-strong', 'password-very-strong');
            const classes = ['password-weak', 'password-medium', 'password-strong', 'password-very-strong'];
            if (level > 0) strengthContainer.classList.add(classes[Math.min(level - 1, 3)]);

            // Update text and color
            const strengths = ['Weak', 'Fair', 'Good', 'Strong'];
            strengthText.textContent = password ? strengths[Math.min(level, 3)] : 'Weak';
            strengthText.className = password ? (level === 3 ? 'text-success' : level === 2 ? 'text-warning' : 'text-danger') : 'text-muted';

            // Update progress bar
            progressBar.style.width = (score / 6 * 100) + '%';
            progressBar.className = 'progress-bar'; // Reset class
            if (level > 0) {
                progressBar.classList.add(level === 3 ? 'bg-success' : level === 2 ? 'bg-warning' : 'bg-danger');
            } else {
                progressBar.classList.add('bg-secondary');
            }
        });
    }

    // Password confirmation matching
    const confirmPassword = document.getElementById('signupConfirmPassword');
    const passwordMatch = document.getElementById('passwordMatch');

    if (confirmPassword && passwordMatch && passwordInput) {
        function checkPasswordMatch() {
            const password = passwordInput.value;
            const confirm = confirmPassword.value;
            const xIcon = passwordMatch.querySelector('.bi-x-circle');
            const checkIcon = passwordMatch.querySelector('.bi-check-circle');

            if (confirm === '') {
                xIcon.style.display = 'none';
                checkIcon.style.display = 'none';
                confirmPassword.classList.remove('is-valid', 'is-invalid');
            } else if (password === confirm && password.length > 0) {
                xIcon.style.display = 'none';
                checkIcon.style.display = 'inline';
                confirmPassword.classList.add('is-valid');
                confirmPassword.classList.remove('is-invalid');
            } else {
                xIcon.style.display = 'inline';
                checkIcon.style.display = 'none';
                confirmPassword.classList.add('is-invalid');
                confirmPassword.classList.remove('is-valid');
            }
        }

        confirmPassword.addEventListener('input', checkPasswordMatch);
        passwordInput.addEventListener('input', checkPasswordMatch);
    }

    // Form submission
    const signupForm = document.getElementById('signupForm');
    if (signupForm) {
        signupForm.addEventListener('submit', function(e) {
            const signupPassword = document.getElementById('signupPassword');
            const signupConfirmPassword = document.getElementById('signupConfirmPassword');
            const submitBtn = this.querySelector('button[type="submit"]');

            if (signupPassword.value !== signupConfirmPassword.value) {
                e.preventDefault();
                alert('Passwords do not match!');
                return;
            }
            

            // Change button text to "Signing up..." and submit form
            const originalText = submitBtn.textContent;
            submitBtn.textContent = 'Signing up...';


            // Submit the form to the server
            this.submit(); // This will trigger the POST to /signup
        });
    }
});






// Check user status on page load// This function checks if the user is logged in and updates the UI accordingly
// This function checks if the user is logged in and updates the UI accordingly


document.addEventListener('DOMContentLoaded', function() {
    checkUserStatus();
});

async function checkUserStatus(){
    try {
        const response = await fetch('/auth/status');
        const result = await response.json();

        const userActions = document.getElementById('user-actions');
        const dashboardNav = document.getElementById('dashboard-nav');
        const dashboardLink = document.getElementById('dashboard-link');
        const subscriptionNav = document.getElementById('subscription-nav');

        if (result.loggedIn) {
            // User is logged in - show dropdown with user menu
            userActions.innerHTML = `
                <div class="dropdown">
                    <button class="btn btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle me-2"></i>${result.userName}
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><h6 class="dropdown-header">
                            <i class="bi bi-person me-2"></i>${result.userName}
                            <small class="text-muted d-block">${result.email}</small>
                        </h6></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="${getDashboardUrl(result.role)}">
                            <i class="bi bi-speedometer2 me-2"></i>Dashboard
                        </a></li>
                       
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="/logout">
                            <i class="bi bi-box-arrow-right me-2"></i>Logout
                        </a></li>
                    </ul>
                </div>
            `;

            // Show dashboard and subscription in main nav for logged in users
            if (dashboardNav && dashboardLink) {
                dashboardNav.style.display = 'block';
                dashboardLink.href = getDashboardUrl(result.role);
            }

            if (subscriptionNav) {
                subscriptionNav.style.display = 'block';
            }

        } else {
            // User not logged in - show login/signup buttons
            userActions.innerHTML = `
                <a href="/login" class="btn btn-outline-primary me-2">
                    <i class="bi bi-box-arrow-in-right me-1"></i>Login
                </a>
                <a href="/signup" class="btn btn-primary">
                    <i class="bi bi-person-plus me-1"></i>Sign Up
                </a>
            `;

            // Hide user-specific nav items
            if (dashboardNav) dashboardNav.style.display = 'none';
            if (subscriptionNav) subscriptionNav.style.display = 'none';
        }

        // Add active class to current page
        highlightActivePage();

    } catch (error) {
        console.error('Error checking user status:', error);
    }
}

function getDashboardUrl(role) {
    return role === 'ADMIN' ? '/admin-dashboard' : '/user-dashboard';
}

function highlightActivePage() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');

    navLinks.forEach(link => {
        const linkPath = new URL(link.href).pathname;
        if (linkPath === currentPath) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}