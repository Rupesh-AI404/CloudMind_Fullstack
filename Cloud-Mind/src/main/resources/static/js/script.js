// Theme Toggle Functionality
document.addEventListener('DOMContentLoaded', function() {
    const themeToggle = document.getElementById('themeToggle');
    const lightIcon = document.getElementById('lightIcon');
    const darkIcon = document.getElementById('darkIcon');
    const body = document.body;

    // Apply saved theme or default to light mode
    const savedTheme = localStorage.getItem('theme') || 'light';
    body.setAttribute('data-bs-theme', savedTheme);
    if (savedTheme === 'dark') {
        lightIcon.classList.add('d-none');
        darkIcon.classList.remove('d-none');
    } else {
        lightIcon.classList.remove('d-none');
        darkIcon.classList.add('d-none');
    }

    // Theme toggle event listener
    themeToggle.addEventListener('click', function() {
        const currentTheme = body.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        body.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);

        if (newTheme === 'dark') {
            lightIcon.classList.add('d-none');
            darkIcon.classList.remove('d-none');
        } else {
            lightIcon.classList.remove('d-none');
            darkIcon.classList.add('d-none');
        }
    });
});

// Auto theme switcher for [data-bs-theme] on <html>
function setThemeBySystem() {
  const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  document.documentElement.setAttribute('data-bs-theme', isDark ? 'dark' : 'light');
}
setThemeBySystem();
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', setThemeBySystem);


// Smooth scrolling for anchor links
document.addEventListener('DOMContentLoaded', function() {
    const links = document.querySelectorAll('a[href^="#"]');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                const offsetTop = targetElement.offsetTop - 80; // Account for fixed navbar
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
});

// Navbar background change on scroll
document.addEventListener('DOMContentLoaded', function() {
    const navbar = document.querySelector('.navbar');
    
    window.addEventListener('scroll', function() {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
});

// Animation on scroll (optional enhancement)
document.addEventListener('DOMContentLoaded', function() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Observe elements with fade-in animation
    const animatedElements = document.querySelectorAll('.service-card, .pricing-card, .contact-item');
    animatedElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
});

// --- THEME MODE FIX ---
const themeToggleBtn = document.getElementById('themeToggle');
const lightIcon = document.getElementById('lightIcon');
const darkIcon = document.getElementById('darkIcon');

function applyTheme(theme) {
  document.documentElement.setAttribute('data-bs-theme', theme);
  if (lightIcon && darkIcon) {
    if (theme === 'dark') {
      lightIcon.classList.add('d-none');
      darkIcon.classList.remove('d-none');
    } else {
      lightIcon.classList.remove('d-none');
      darkIcon.classList.add('d-none');
    }
  }
}

function getPreferredTheme() {
  return localStorage.getItem('theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
}

function setTheme(theme) {
  localStorage.setItem('theme', theme);
  applyTheme(theme);
}

// On load, set theme
applyTheme(getPreferredTheme());

// Listen for OS theme changes if user hasn't chosen
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
  if (!localStorage.getItem('theme')) {
    applyTheme(e.matches ? 'dark' : 'light');
  }
});

// Theme toggle button
if (themeToggleBtn) {
  themeToggleBtn.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-bs-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    setTheme(next);
  });
}
// --- END THEME MODE FIX ---

// === ADMIN DASHBOARD FUNCTIONALITY ===

// Purchase tracking and subscription management functions
function exportPurchaseData() {
    // Simulate data export
    const data = [
        ['User', 'Package', 'Purchase Date', 'Start Date', 'End Date', 'Status', 'Revenue'],
        ['John Doe', 'Pro', '2024-12-15', '2025-01-01', '2025-12-31', 'Active', '$29,988'],
        ['Sarah Miller', 'Starter', '2024-11-28', '2024-12-01', '2025-05-31', 'Active', '$5,994'],
        ['Mike Johnson', 'Enterprise', '2024-12-08', '2024-12-15', '2025-12-14', 'Pending', '$59,988'],
        ['Alice Cooper', 'Pro', '2024-10-01', '2024-10-01', '2024-12-31', 'Expired', '$7,497']
    ];
    
    const csvContent = data.map(row => row.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'purchase_data_' + new Date().toISOString().split('T')[0] + '.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    
    // Show success message
    showNotification('Purchase data exported successfully!', 'success');
}

function refreshPurchaseData() {
    // Simulate data refresh
    showNotification('Refreshing purchase data...', 'info');
    
    setTimeout(() => {
        showNotification('Purchase data refreshed successfully!', 'success');
        // In a real application, this would reload the table data
    }, 1500);
}

function viewUserDetails(userId) {
    // Simulate opening user details modal
    showNotification(`Opening details for user ${userId}...`, 'info');
    
    // In a real application, this would open a modal with detailed user information
    setTimeout(() => {
        alert(`User Details for ${userId}:\n\n• Purchase History\n• Payment Methods\n• Subscription Timeline\n• Support Tickets\n• Usage Analytics`);
    }, 500);
}

function editSubscription(userId) {
    // Simulate opening subscription edit modal
    showNotification(`Opening subscription editor for user ${userId}...`, 'info');
    
    setTimeout(() => {
        const action = confirm(`Edit subscription for user ${userId}?\n\nOptions:\n• Change package\n• Modify dates\n• Update billing\n• Toggle auto-renewal\n\nProceed?`);
        if (action) {
            showNotification(`Subscription updated for user ${userId}`, 'success');
        }
    }, 500);
}

function cancelSubscription(userId) {
    // Simulate subscription cancellation
    const confirmed = confirm(`Are you sure you want to cancel the subscription for user ${userId}?\n\nThis action cannot be undone.`);
    
    if (confirmed) {
        showNotification(`Cancelling subscription for user ${userId}...`, 'warning');
        
        setTimeout(() => {
            showNotification(`Subscription cancelled for user ${userId}`, 'success');
            // In a real application, this would update the table row
        }, 1000);
    }
}

function activateSubscription(userId) {
    // Simulate subscription activation
    const confirmed = confirm(`Activate subscription for user ${userId}?\n\nThis will process the pending payment and start the subscription.`);
    
    if (confirmed) {
        showNotification(`Activating subscription for user ${userId}...`, 'info');
        
        setTimeout(() => {
            showNotification(`Subscription activated for user ${userId}`, 'success');
            // In a real application, this would update the table row
        }, 1500);
    }
}

function renewSubscription(userId) {
    // Simulate subscription renewal
    const confirmed = confirm(`Renew subscription for user ${userId}?\n\nThis will extend the subscription for another billing period.`);
    
    if (confirmed) {
        showNotification(`Processing renewal for user ${userId}...`, 'info');
        
        setTimeout(() => {
            showNotification(`Subscription renewed for user ${userId}`, 'success');
            // In a real application, this would update the table row
        }, 1500);
    }
}

function sendRenewalReminder(userId) {
    // Simulate sending renewal reminder
    showNotification(`Sending renewal reminder to user ${userId}...`, 'info');
    
    setTimeout(() => {
        showNotification(`Renewal reminder sent to user ${userId}`, 'success');
    }, 1000);
}

// Filter functionality
document.addEventListener('DOMContentLoaded', function() {
    const packageFilter = document.getElementById('packageFilter');
    const statusFilter = document.getElementById('statusFilter');
    const dateFilter = document.getElementById('dateFilter');
    const searchFilter = document.getElementById('searchFilter');
    const purchaseTable = document.getElementById('purchaseTable');
    
    if (packageFilter && statusFilter && dateFilter && searchFilter && purchaseTable) {
        // Add event listeners for filters
        [packageFilter, statusFilter, dateFilter, searchFilter].forEach(filter => {
            filter.addEventListener('change', applyFilters);
            filter.addEventListener('input', applyFilters);
        });
        
        function applyFilters() {
            const packageValue = packageFilter.value.toLowerCase();
            const statusValue = statusFilter.value.toLowerCase();
            const dateValue = dateFilter.value;
            const searchValue = searchFilter.value.toLowerCase();
            
            const rows = purchaseTable.querySelectorAll('tbody tr');
            
            rows.forEach(row => {
                const packageText = row.cells[1].textContent.toLowerCase();
                const statusText = row.cells[6].textContent.toLowerCase();
                const userText = row.cells[0].textContent.toLowerCase();
                const purchaseDate = row.cells[2].textContent;
                
                let showRow = true;
                
                // Package filter
                if (packageValue !== 'all' && !packageText.includes(packageValue)) {
                    showRow = false;
                }
                
                // Status filter
                if (statusValue !== 'all' && !statusText.includes(statusValue)) {
                    showRow = false;
                }
                
                // Search filter
                if (searchValue && !userText.includes(searchValue)) {
                    showRow = false;
                }
                
                // Date filter (basic implementation)
                if (dateValue && !purchaseDate.includes(dateValue.split('-')[0])) {
                    // Simple year matching - in real app would be more sophisticated
                    showRow = false;
                }
                
                row.style.display = showRow ? '' : 'none';
            });
            
            // Update showing count
            const visibleRows = Array.from(rows).filter(row => row.style.display !== 'none').length;
            const totalRows = rows.length;
            const showingText = document.querySelector('.small.text-muted');
            if (showingText) {
                showingText.textContent = `Showing ${visibleRows} of ${totalRows} users`;
            }
        }
    }
});

// Notification system
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(notification);
    
    // Auto remove after 4 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 4000);
}

// Revenue chart interaction (basic hover effects)
document.addEventListener('DOMContentLoaded', function() {
    const chartBars = document.querySelectorAll('.chart-bar');
    
    chartBars.forEach(bar => {
        bar.style.cursor = 'pointer';
        bar.style.transition = 'all 0.3s ease';
        
        bar.addEventListener('mouseenter', function() {
            this.style.opacity = '0.8';
            this.style.transform = 'scale(1.05)';
            
            // Show tooltip if title attribute exists
            if (this.title) {
                showTooltip(this, this.title);
            }
        });
        
        bar.addEventListener('mouseleave', function() {
            this.style.opacity = '1';
            this.style.transform = 'scale(1)';
            hideTooltip();
        });
    });
});

// Simple tooltip system
let tooltip = null;

function showTooltip(element, text) {
    hideTooltip();
    
    tooltip = document.createElement('div');
    tooltip.className = 'tooltip-custom';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: rgba(0,0,0,0.8);
        color: white;
        padding: 5px 10px;
        border-radius: 4px;
        font-size: 12px;
        z-index: 10000;
        pointer-events: none;
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + rect.width / 2 - tooltip.offsetWidth / 2 + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 5 + 'px';
}

function hideTooltip() {
    if (tooltip) {
        tooltip.remove();
        tooltip = null;
    }
}

// === END ADMIN DASHBOARD FUNCTIONALITY ===

// === USER DASHBOARD FUNCTIONALITY ===

// User subscription management functions
function refreshSubscriptionData() {
    showNotification('Refreshing subscription data...', 'info');
    
    setTimeout(() => {
        showNotification('Subscription data refreshed!', 'success');
        // In a real application, this would reload subscription data from backend
    }, 1500);
}

function renewSubscription() {
    const confirmed = confirm('Renew your subscription for another billing period?\n\nThis will extend your current plan and charge your payment method.');
    
    if (confirmed) {
        showNotification('Processing subscription renewal...', 'info');
        
        setTimeout(() => {
            showNotification('Subscription renewed successfully!', 'success');
            // In a real application, this would process the renewal
        }, 2000);
    }
}

function upgradeSubscription() {
    const confirmed = confirm('Upgrade to a higher plan?\n\nYou will be redirected to our pricing page to choose a new plan.');
    
    if (confirmed) {
        showNotification('Redirecting to upgrade options...', 'info');
        
        setTimeout(() => {
            window.location.href = 'pricing.html';
        }, 1000);
    }
}

function viewSubscriptionDetails() {
    // Simulate opening subscription details modal
    showNotification('Loading subscription details...', 'info');
    
    setTimeout(() => {
        alert(`Subscription Details:\n\n• Plan: Pro Plan\n• Status: Active\n• Purchase Date: Dec 15, 2024\n• Start Date: Jan 1, 2025\n• End Date: Dec 31, 2025\n• Auto-renewal: OFF\n• Payment Method: **** 1234\n• Next Billing: Dec 31, 2025`);
    }, 500);
}

function toggleAutoRenewal() {
    const currentStatus = document.getElementById('autoRenewalStatus')?.textContent || 'OFF';
    const newStatus = currentStatus === 'ON' ? 'OFF' : 'ON';
    
    const confirmed = confirm(`${newStatus === 'ON' ? 'Enable' : 'Disable'} auto-renewal?\n\n${newStatus === 'ON' ? 'Your subscription will automatically renew before expiration.' : 'You will need to manually renew your subscription.'}`);
    
    if (confirmed) {
        showNotification(`Auto-renewal ${newStatus === 'ON' ? 'enabled' : 'disabled'}`, 'success');
        
        // Update UI if elements exist
        const statusElement = document.getElementById('autoRenewalStatus');
        if (statusElement) {
            statusElement.textContent = newStatus;
            statusElement.className = `small fw-bold ${newStatus === 'ON' ? 'text-success' : 'text-muted'}`;
        }
    }
}

function renewExpiredSubscription() {
    const confirmed = confirm('Renew your expired subscription?\n\nThis will reactivate your plan and charge your payment method.');
    
    if (confirmed) {
        showNotification('Processing subscription renewal...', 'info');
        
        setTimeout(() => {
            showNotification('Subscription renewed and reactivated!', 'success');
            // In a real application, this would switch from expired to active state
            switchSubscriptionState('active');
        }, 2000);
    }
}

// Subscription state management
function switchSubscriptionState(state) {
    const noSubState = document.getElementById('noSubscriptionState');
    const activeState = document.getElementById('activeSubscriptionState');
    const expiredState = document.getElementById('expiredSubscriptionState');
    
    if (!noSubState || !activeState || !expiredState) return;
    
    // Hide all states
    noSubState.classList.add('d-none');
    activeState.classList.add('d-none');
    expiredState.classList.add('d-none');
    
    // Show appropriate state
    switch(state) {
        case 'none':
            noSubState.classList.remove('d-none');
            break;
        case 'active':
            activeState.classList.remove('d-none');
            populateActiveSubscriptionData();
            break;
        case 'expired':
            expiredState.classList.remove('d-none');
            break;
    }
}

// Populate subscription data (for demonstration)
function populateActiveSubscriptionData() {
    const elements = {
        planName: document.getElementById('planName'),
        planDescription: document.getElementById('planDescription'),
        planStatus: document.getElementById('planStatus'),
        daysLeft: document.getElementById('daysLeft'),
        monthlyPrice: document.getElementById('monthlyPrice'),
        timeProgress: document.getElementById('timeProgress'),
        timeProgressBar: document.getElementById('timeProgressBar'),
        startDate: document.getElementById('startDate'),
        endDate: document.getElementById('endDate'),
        campaignsUsed: document.getElementById('campaignsUsed'),
        campaignsProgressBar: document.getElementById('campaignsProgressBar'),
        apiCallsUsed: document.getElementById('apiCallsUsed'),
        apiCallsProgressBar: document.getElementById('apiCallsProgressBar'),
        autoRenewalStatus: document.getElementById('autoRenewalStatus')
    };
    
    // Sample data (in real app, this would come from backend)
    const subscriptionData = {
        planName: 'Pro Plan',
        planDescription: 'Perfect for growing businesses',
        status: 'Active',
        daysLeft: 355,
        monthlyPrice: '$2,499',
        timeProgress: 3, // 3% of subscription used
        startDate: 'Jan 1, 2025',
        endDate: 'Dec 31, 2025',
        campaignsUsed: 8,
        campaignsLimit: 15,
        apiCallsUsed: 2400,
        apiCallsLimit: 5000,
        autoRenewal: false
    };
    
    // Update elements if they exist
    if (elements.planName) elements.planName.textContent = subscriptionData.planName;
    if (elements.planDescription) elements.planDescription.textContent = subscriptionData.planDescription;
    if (elements.planStatus) elements.planStatus.textContent = subscriptionData.status;
    if (elements.daysLeft) elements.daysLeft.textContent = subscriptionData.daysLeft;
    if (elements.monthlyPrice) elements.monthlyPrice.textContent = subscriptionData.monthlyPrice;
    if (elements.timeProgress) elements.timeProgress.textContent = subscriptionData.timeProgress + '%';
    if (elements.timeProgressBar) elements.timeProgressBar.style.width = subscriptionData.timeProgress + '%';
    if (elements.startDate) elements.startDate.textContent = subscriptionData.startDate;
    if (elements.endDate) elements.endDate.textContent = subscriptionData.endDate;
    if (elements.campaignsUsed) elements.campaignsUsed.textContent = `${subscriptionData.campaignsUsed}/${subscriptionData.campaignsLimit}`;
    if (elements.campaignsProgressBar) elements.campaignsProgressBar.style.width = (subscriptionData.campaignsUsed / subscriptionData.campaignsLimit * 100) + '%';
    if (elements.apiCallsUsed) elements.apiCallsUsed.textContent = `${subscriptionData.apiCallsUsed.toLocaleString()}/${subscriptionData.apiCallsLimit.toLocaleString()}`;
    if (elements.apiCallsProgressBar) elements.apiCallsProgressBar.style.width = (subscriptionData.apiCallsUsed / subscriptionData.apiCallsLimit * 100) + '%';
    if (elements.autoRenewalStatus) {
        elements.autoRenewalStatus.textContent = subscriptionData.autoRenewal ? 'ON' : 'OFF';
        elements.autoRenewalStatus.className = `small fw-bold ${subscriptionData.autoRenewal ? 'text-success' : 'text-muted'}`;
    }
}

// Demo functions for testing different subscription states
function demoActiveSubscription() {
    switchSubscriptionState('active');
    showNotification('Demo: Showing active subscription state', 'info');
}

function demoExpiredSubscription() {
    switchSubscriptionState('expired');
    const expiredDateElement = document.getElementById('expiredDate');
    if (expiredDateElement) {
        expiredDateElement.textContent = 'Dec 31, 2024 (10 days ago)';
    }
    showNotification('Demo: Showing expired subscription state', 'warning');
}

function demoNoSubscription() {
    switchSubscriptionState('none');
    showNotification('Demo: Showing no subscription state', 'info');
}

// Initialize user dashboard on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the user dashboard page
    if (window.location.pathname.includes('user-dashboard.html')) {
        // Start with no subscription state (fresh start)
        switchSubscriptionState('none');
        
        // Add demo buttons for testing (remove in production)
        if (document.querySelector('.container')) {
            const demoControls = document.createElement('div');
            demoControls.className = 'position-fixed bottom-0 end-0 p-3';
            demoControls.style.zIndex = '1000';
            demoControls.innerHTML = `
                <div class="btn-group-vertical" role="group">
                    <button class="btn btn-sm btn-outline-secondary" onclick="demoNoSubscription()" title="Demo: No Subscription">No Sub</button>
                    <button class="btn btn-sm btn-outline-success" onclick="demoActiveSubscription()" title="Demo: Active Subscription">Active</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="demoExpiredSubscription()" title="Demo: Expired Subscription">Expired</button>
                </div>
            `;
            document.body.appendChild(demoControls);
        }
    }
});

// === END USER DASHBOARD FUNCTIONALITY ===