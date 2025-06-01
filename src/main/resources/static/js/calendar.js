document.addEventListener('DOMContentLoaded', function () {
    var calendarEl = document.getElementById('calendar');

    var calendar = new FullCalendar.Calendar(calendarEl, {
        height: 'auto',
        initialView: 'dayGridWeek',
        locale: 'es',
        firstDay: 1,
        buttonText: {
            today: 'Hoy',
            month: 'Mes',
            week: 'Semana',
            day: 'Día'
        },
        events: '/api/calendario/eventos',
        eventContent: function (arg) {
            const start = new Date(arg.event.start);
            const end = new Date(arg.event.end);

            const horaInicio = start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            const horaFin = end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

            let title = arg.event.title || '';
            let styledTitle = title;

            if (title.includes('Tuya')) {
                styledTitle = title.replace('Tuya', '<span style="color: #28a745;">Tuya</span>');
            } else if (title.includes('Ocupado')) {
                styledTitle = title.replace('Ocupado', '<span style="color: #a7288a;">Ocupado</span>');
            }

            const contenido = `<div><b>${horaInicio} - ${horaFin}</b><br>${styledTitle}</div>`;
            return { html: contenido };
        }
    });

    calendar.render();
});
