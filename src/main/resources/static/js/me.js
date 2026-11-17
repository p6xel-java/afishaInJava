
(() => {
    'use strict';

    const tokenKey = 'accessToken';
    const $ = id => document.getElementById(id);
    let user = null;

    const setText = (id, value) => { $(id).textContent = value == null || value === '' ? '—' : String(value); };
    const isAdmin = () => user?.role === 'ROLE_ADMIN';

    async function api(path, { method = 'GET', body } = {}) {
        const token = sessionStorage.getItem(tokenKey);
        if (!token) throw Object.assign(new Error('Чтобы открыть профиль, сначала войди в аккаунт на главной странице.'), { status: 401 });

        let response;
        try {
            response = await fetch(`/api/v1${path}`, {
                method,
                headers: {
                    'Authorization': `Bearer ${token}`,
                    ...(body !== undefined ? { 'Content-Type': 'application/json' } : {})
                },
                ...(body !== undefined ? { body: JSON.stringify(body) } : {})
            });
        } catch (_) {
            throw new Error('Не удалось связаться с сервером. Проверь, что backend запущен.');
        }
        if (!response.ok) {
            let details = {};
            try { details = await response.json(); } catch (_) { /* ответ без JSON */ }
            const fieldErrors = details?.fieldErrors ? Object.values(details.fieldErrors).join('; ') : '';
            throw Object.assign(new Error(fieldErrors || details?.message || `Ошибка сервера: ${response.status}`), { status: response.status });
        }
        if (response.status === 204) return null;
        return response.json();
    }

    function showTab(name) {
        if (!user) return;
        // /me without a hash opens the profile by default.
        // Unknown tabs and unauthorized admin tabs also fall back to profile.
        if (name !== 'admin' || !isAdmin()) name = 'profile';
        $('profileSection').hidden = name !== 'profile';
        $('adminSection').hidden = name !== 'admin';
        document.querySelectorAll('button[data-tab]').forEach(button => {
            const active = button.dataset.tab === name;
            button.classList.toggle('active', active);
            button.setAttribute('aria-pressed', String(active));
        });
        const hash = name === 'admin' ? '#admin' : '#profile';
        if (location.hash !== hash) {
            history.replaceState(null, '', location.pathname + location.search + hash);
        }
    }

    function renderProfile() {
        const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || 'Пользователь';
        const initial = (user.firstName || user.email || 'А').trim().charAt(0).toUpperCase();
        const roleName = isAdmin() ? 'Администратор' : 'Пользователь';

        setText('sideAvatar', initial);
        setText('profileAvatar', initial);
        setText('sideName', fullName);
        setText('sideEmail', user.email);
        setText('fullName', fullName);
        setText('primaryEmail', user.email);
        setText('firstName', user.firstName);
        setText('lastName', user.lastName);
        setText('email', user.email);
        setText('role', roleName);
        setText('rolePill', roleName);
        setText('userId', user.id == null ? '—' : `#${user.id}`);

        $('adminTab').hidden = !isAdmin();
        $('adminShortcut').hidden = !isAdmin();
        $('rolePill').classList.toggle('is-admin', isAdmin());
        $('loadingCard').hidden = true;
        $('errorCard').hidden = true;
        showTab(location.hash === '#admin' && isAdmin() ? 'admin' : 'profile');
    }

    async function loadProfile() {
        $('loadingCard').hidden = false;
        $('errorCard').hidden = true;
        $('profileSection').hidden = true;
        $('adminSection').hidden = true;
        try {
            user = await api('/users/me');
            renderProfile();
        } catch (err) {
            user = null;
            $('loadingCard').hidden = true;
            $('errorCard').hidden = false;
            $('errorText').textContent = err.message;
            if (err.status === 401) {
                $('retryBtn').hidden = true;
            } else {
                $('retryBtn').hidden = false;
            }
        }
    }

    function localDateTimeNow() {
        const now = new Date();
        return new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
    }

    function showFormMessage(type, message) {
        $('formError').hidden = type !== 'error';
        $('formSuccess').hidden = type !== 'success';
        $(type === 'error' ? 'formError' : 'formSuccess').textContent = message;
    }

    async function createEvent(event) {
        event.preventDefault();
        $('formError').hidden = true;
        $('formSuccess').hidden = true;
        if (!isAdmin()) { showFormMessage('error', 'У этого аккаунта нет прав администратора.'); return; }

        const form = event.currentTarget;
        const formData = new FormData(form);
        const title = String(formData.get('title') || '').trim();
        const venue = String(formData.get('venue') || '').trim();
        const price = String(formData.get('price') || '');
        const startDateTime = String(formData.get('startDateTime') || '');
        if (!title || !venue || !startDateTime || price === '' || !Number.isFinite(Number(price)) || Number(price) < 0) {
            showFormMessage('error', 'Проверь обязательные поля и стоимость.');
            return;
        }
        if (new Date(startDateTime).getTime() < Date.now()) {
            showFormMessage('error', 'Укажи дату мероприятия в будущем.');
            return;
        }

        const submit = $('createSubmit');
        submit.disabled = true;
        submit.textContent = 'Публикуем…';
        try {
            const result = await api('/events', {
                method: 'POST',
                body: {
                    title,
                    description: String(formData.get('description') || '').trim(),
                    venue,
                    price,
                    startDateTime
                }
            });
            showFormMessage('success', `Событие «${result?.title || title}» создано${result?.id ? ` (ID: ${result.id})` : ''}! Оно доступно в каталоге.`);
            form.reset();
            form.elements.startDateTime.min = localDateTimeNow();
        } catch (err) {
            showFormMessage('error', err.message);
        } finally {
            submit.disabled = false;
            submit.textContent = 'Опубликовать событие ↗';
        }
    }

    document.querySelectorAll('button[data-tab]').forEach(button => button.addEventListener('click', () => showTab(button.dataset.tab)));
    $('adminShortcut').addEventListener('click', () => showTab('admin'));
    $('retryBtn').addEventListener('click', loadProfile);
    $('createEventForm').addEventListener('submit', createEvent);
    $('createEventForm').elements.startDateTime.min = localDateTimeNow();
    $('logoutBtn').addEventListener('click', () => { sessionStorage.removeItem(tokenKey); location.assign('/'); });
    window.addEventListener('hashchange', () => {
        if (user) showTab(location.hash === '#admin' ? 'admin' : 'profile');
    });
    loadProfile();
})();
