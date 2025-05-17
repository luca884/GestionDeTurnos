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
            userInfo.textContent = `Rol: ${role}, Email: ${email}`;

            // aca se puede agregar el boton de logout
            const logoutBtn = document.createElement('button');
            logoutBtn.textContent = 'Salir';
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
            loginBtn.textContent = 'Iniciar sesión';
            loginBtn.style.marginRight = '10px';

            const registerBtn = document.createElement('a');
            registerBtn.id = 'register-btn';
            registerBtn.href = '/register';
            registerBtn.textContent = 'Registrarse';

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
                    console.error('Error de servedor: ', errorText);
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
