document.addEventListener('DOMContentLoaded', () => {

    async function fetchUserInfo() {
        try {
            const response = await fetch('/api/auth/me', {
                method: 'GET',
                credentials: 'include'
            });
            if (!response.ok) {
                return null;
            }
            return await response.json();
        } catch (error) {
            console.error('Error fetching user info:', error);
            return null;
        }
    }

    async function updateUI() {
        const headerRight = document.getElementById('header-right');
        if (!headerRight) return;

        headerRight.innerHTML = '';

        const userInfo = await fetchUserInfo();

        if (userInfo && userInfo.email && userInfo.role) {
            const userDiv = document.createElement('div');
            userDiv.id = 'user-info';
            userDiv.style.display = 'flex';
            userDiv.style.alignItems = 'center';
            userDiv.style.gap = '15px';

            const roleElem = document.createElement('span');
            roleElem.className = 'user-role';
            roleElem.textContent = userInfo.role;

            const emailElem = document.createElement('span');
            emailElem.className = 'user-email';
            emailElem.textContent = userInfo.email;

            userDiv.appendChild(roleElem);
            userDiv.appendChild(emailElem);

            const logoutBtn = document.createElement('button');
            logoutBtn.className = 'logout-btn';
            logoutBtn.innerHTML = `Salir<i class="ri-logout-circle-line" style="margin-right: 6px; vertical-align: middle;"></i>`;

            logoutBtn.addEventListener('click', async () => {
                try {
                    const response = await fetch('/api/auth/logout', {
                        method: 'POST',
                        credentials: 'include'
                    });
                    if (response.ok) {
                        updateUI();
                        window.location.href = '/';
                    } else {
                        alert('Error al cerrar sesión.');
                    }
                } catch (error) {
                    console.error('Logout error:', error);
                    alert('Error al cerrar sesión.');
                }
            });


            headerRight.appendChild(userDiv);
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

    // login
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
                    body: JSON.stringify({email, password}),
                    credentials: 'include'
                });

                if (!response.ok) {
                    alert('Email o contraseña incorrectos.');
                    return;
                }
                const userInfo = await fetchUserInfo();

                if (userInfo && userInfo.role) {
                    if (userInfo.role === 'CLIENTE') {
                        window.location.href = '/cliente/reservas';
                    } else if (userInfo.role === 'ADMIN') {
                        window.location.href = '/admin/empleados';
                    } else if (userInfo.role === 'EMPLEADO') {
                        window.location.href = '/empleado';
                    } else {
                        window.location.href = '/';
                    }
                } else {
                    window.location.href = '/';
                }
            } catch (error) {
                alert('Error del servidor, intenta más tarde.');
            }
        });
    }
});


// crear reserva

document.addEventListener("DOMContentLoaded", function () {
    const horaInicio = document.getElementById("horaInicio");
    const horaFin = document.getElementById("horaFin");
    const salaSelect = document.getElementById("salaId");
    const reservaForm = document.getElementById("reservaForm");

    if (!horaInicio || !horaFin || !salaSelect || !reservaForm) {
        console.error("No encontre los elementos necesarios para crear la reserva.");
        return;
    }

    for (let h = 8; h <= 22; h++) {
        const display = h.toString().padStart(2, '0') + ":00";
        horaInicio.add(new Option(display, h));
        horaFin.add(new Option(display, h));
    }

    fetch("/api/salas")
        .then(response => {
            if (!response.ok) throw new Error("Error al cargar salas");
            return response.json();
        })
        .then(data => {
            salaSelect.length = 1;
            data.forEach(sala => {
                const descripcion = sala.descripcion ? ` - ${sala.descripcion}` : "";
                const label = `Sala ${sala.numero}(capacidad: ${sala.cantPersonas})${descripcion}`;
                salaSelect.add(new Option(label, sala.id));
            });
        })
        .catch(error => {
            console.error("Error al cargar salas:", error);
        });


    reservaForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const inicio = parseInt(horaInicio.value);
        const fin = parseInt(horaFin.value);

        if (fin <= inicio) {
            alert("La hora de fin debe ser mayor que la hora de inicio.");
            return;
        }
        const fecha = document.getElementById("fecha").value;
        if (fecha < new Date().toISOString().split('T')[0]) {
            alert("La fecha no puede ser anterior a hoy.");
            return;
        }
        const fechaInicio = `${fecha}T${horaInicio.value.toString().padStart(2, '0')}:00:00`;
        const fechaFinal = `${fecha}T${horaFin.value.toString().padStart(2, '0')}:00:00`;

        const salaId = parseInt(salaSelect.value);
        if (!salaId) {
            alert("Por favor, selecciona una sala.");
            return;
        }

        const tipoPago = document.getElementById("tipoPago").value;

        const data = {
            salaId: salaId,
            fechaInicio: fechaInicio,
            fechaFinal: fechaFinal,
            tipoPago: tipoPago
        };


        try {
            const response = await fetch('/api/reserva', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                const reserva = result;
                const mensaje = `
Reserva creada correctamente!
Sala: ${reserva.sala.numero} (Capacidad: ${reserva.sala.cantPersonas})
Fecha inicio: ${reserva.fechaInicio.replace('T', ' ')}
Fecha fin: ${reserva.fechaFinal.replace('T', ' ')}
Tipo de pago: ${reserva.tipoPago}
                `;
                document.getElementById("reservaResult").innerText = mensaje;

            } else {
                let errorMessage = 'Error al crear la reserva.';
                if (result && typeof result === 'string') {
                    errorMessage = result;
                } else if (result && result.message) {
                    errorMessage = result.message;
                }
                if (errorMessage.includes('superpone')) {
                    errorMessage = 'La reserva no se puede crear porque el horario ya está ocupado.';
                }
                document.getElementById("reservaResult").innerText = errorMessage;
            }
        } catch (error) {
            document.getElementById("reservaResult").innerText = 'Error al enviar la solicitud.';
            console.error('Fetch error:', error);
        }
    });
});


// mostrar reservas activas

document.addEventListener("DOMContentLoaded", async () => {
    const container = document.getElementById("salasReservasContainer");
    if (!container) return;

    try {
        const response = await fetch('/api/salas/cliente/activas', {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Error al cargar reservas activas');

        const reservas = await response.json();

        if (reservas.length === 0) {
            container.textContent = 'No tienes reservas activas.';
            return;
        }

        reservas.forEach(reserva => {
            const fechaInicioFormateada = reserva.fechaInicio.replace('T', ' ').slice(0, 16);
            const fechaFinalFormateada = reserva.fechaFinal.replace('T', ' ').slice(0, 16);

            const div = document.createElement('div');
            div.className = 'reserva-item';
            div.textContent = `Sala ${reserva.salaNumero} (Capacidad: ${reserva.salaCapacidad}) - ` +
                `${fechaInicioFormateada} a ${fechaFinalFormateada} - ` +
                `Pago: ${reserva.tipoPago} - Estado: ${reserva.estado}`;
            container.appendChild(div);
        });

    } catch (error) {
        console.error(error);
        container.textContent = 'Error al cargar las reservas.';
    }
});

// mostrar empleados
document.addEventListener("DOMContentLoaded", async () => {
    const container = document.getElementById("empleadosContainer");
    if (!container) return;

    try {
        const response = await fetch('/api/empleados', {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Error al cargar empleados');

        const empleados = await response.json();

        if (empleados.length === 0) {
            container.textContent = 'No hay empleados.';
            return;
        }

        empleados.forEach(empleado => {
            const legajo = empleado.legajo;
            const nombreCompleto = `${empleado.nombre} ${empleado.apellido}`;
            const dni = empleado.dni;
            const email = empleado.email;
            const telefono = empleado.telefono;

            const div = document.createElement('div');
            div.className = 'empleado-item';
            div.textContent = `Legajo: ${legajo} - Nombre: ${nombreCompleto} - DNI: ${dni} - Email: ${email} - Teléfono: ${telefono}`;

            container.appendChild(div);
        });

    } catch (error) {
        console.error(error);
        container.textContent = 'Error al cargar empleados.';
    }
});

// crear empleado

document.addEventListener("DOMContentLoaded", function () {
    const empleadoForm = document.getElementById("empleadoForm");
    if (!empleadoForm) {
        console.error("No encontre el formulario de empleado.");
        return;
    }

    empleadoForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const apellido = document.getElementById("apellido").value.trim();
        const dni = document.getElementById("dni").value.trim();
        const telefono = document.getElementById("telefono").value.trim();
        const email = document.getElementById("email").value.trim();
        const legajo = document.getElementById("legajo").value.trim();
        const rol = document.getElementById("rol").value;
        const contrasena = dni;

        if (!nombre || !apellido || !dni || !telefono || !email || !legajo || !rol) {
            alert("Completa todos los campos.");
            return;
        }

        const data = {
            nombre,
            apellido,
            dni,
            telefono,
            email,
            legajo,
            contrasena,
            rol
        };

        try {
            const response = await fetch('/api/empleados', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            });

            const result = await response.json();
            if (response.ok) {
                const empleado = result;
                const mensaje = `Empleado creado correctamente!`;
                document.getElementById("empleadoResult").innerText = mensaje;
                empleadoForm.reset();
                setTimeout(() => {
                    document.getElementById("empleadoResult").innerText = "";
                }, 5000);

            } else {
                const errorText = await response.text();
                console.error('Respuesta con error:', errorText);
                alert('Error al crear el empleado.');
            }
        } catch (error) {
            console.error('Error al enviar la solicitud:', error);
            alert('Error al enviar la solicitud.');
        }
    });
});

