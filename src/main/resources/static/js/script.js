document.addEventListener('DOMContentLoaded', () => {
    function updateUI() {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        const email = localStorage.getItem('email');

        const headerRight = document.getElementById('header-right');
        if (!headerRight) return;

        headerRight.innerHTML = '';

        if (token) {
            const userInfo = document.createElement('div');
            userInfo.id = 'user-info';
            userInfo.style.display = 'flex';
            userInfo.style.alignItems = 'center';
            userInfo.style.gap = '15px';

            const roleElem = document.createElement('span');
            roleElem.className = 'user-role';
            roleElem.textContent = role || '';

            const emailElem = document.createElement('span');
            emailElem.className = 'user-email';
            emailElem.textContent = email || '';

            userInfo.appendChild(roleElem);
            userInfo.appendChild(emailElem);

            const logoutBtn = document.createElement('button');
            logoutBtn.className = 'logout-btn';
            logoutBtn.innerHTML = `Salir<i class="ri-logout-circle-line" style="margin-right: 6px; vertical-align: middle;"></i>`;
            logoutBtn.addEventListener('click', () => {
                localStorage.clear();
                updateUI();
                window.location.href = '/';
            });

            headerRight.appendChild(userInfo);
            headerRight.appendChild(logoutBtn);
        } else {
            const loginBtn = document.createElement('a');
            loginBtn.id = 'login-btn';
            loginBtn.href = '/login';
            loginBtn.className = 'header-link-btn';

            loginBtn.innerHTML = `<i class="ri-login-circle-line" style="margin-right: 6px; vertical-align: middle;"></i>Iniciar sesión`;

            const registerBtn = document.createElement('a');
            registerBtn.id = 'register-btn';
            registerBtn.href = '/register';
            registerBtn.className = 'header-link-btn';
            registerBtn.innerHTML = `<i class="ri-user-add-fill" style="margin-right: 6px; vertical-align: middle;"></i>Registrarse`;

            headerRight.appendChild(loginBtn);
            headerRight.appendChild(registerBtn);
        }
    }

    updateUI();

    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = loginForm.email.value.trim();
            const password = loginForm.password.value.trim();

            if (!email || !password) {
                alert('Completa todos los campos.');
                return;
            }

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({email, password})
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    console.error('Error de servidor: ', errorText);
                    alert('Email o contraseña incorrectos.');
                    return;
                }

                const data = await response.json();
                localStorage.setItem('token', data.token);
                localStorage.setItem('role', data.rol);
                localStorage.setItem('email', email);

                window.location.href = '/';
            } catch (error) {
                console.error('Error al iniciar sesión:', error);
                alert('Error del servidor, intenta más tarde.');
            }
        });
    }
});
