
(() => {
    'use strict';

    const $ = id => document.getElementById(id);

    const tokenKey = 'accessToken';

    let token = sessionStorage.getItem(tokenKey);
    let events = [];
    let visible = 9;
    let mode = 'login';

    // ==============================
    // УТИЛИТЫ
    // ==============================

    const money = value => {
        if (value === null || value === undefined) {
            return 'Цена не указана';
        }

        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            maximumFractionDigits: 2
        }).format(Number(value));
    };

    const date = value => {
        if (!value) {
            return 'Дата уточняется';
        }

        return new Date(value).toLocaleString('ru-RU', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const text = value => String(value ?? '')
        .replace(/<[^>]*>/g, ' ')
        .replace(/&nbsp;/gi, ' ')
        .replace(/&amp;/gi, '&')
        .replace(/&quot;/gi, '"')
        .replace(/&#39;/gi, "'")
        .replace(/\s+/g, ' ')
        .trim();

    const escapeHtml = value => String(value ?? '')
        .replace(/[&<>"']/g, char => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[char]));

    function toast(message) {
        const element = $('toast');

        element.textContent = message;
        element.classList.add('show');

        clearTimeout(toast.timer);

        toast.timer = setTimeout(() => {
            element.classList.remove('show');
        }, 4500);
    }

    // ==============================
    // REST API
    // ==============================

    async function api(
        path,
        { method = 'GET', body, auth = false } = {}
    ) {
        const headers = {};

        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
        }

        if (auth && token) {
            headers.Authorization = `Bearer ${token}`;
        }

        let response;

        try {
            response = await fetch('/api/v1' + path, {
                method,
                headers,
                body: body === undefined
                    ? undefined
                    : JSON.stringify(body)
            });
        } catch (_) {
            throw new Error(
                'Не удалось подключиться к серверу. Проверьте, запущен ли backend.'
            );
        }

        if (!response.ok) {
            let details;

            try {
                details = await response.json();
            } catch (_) {
                details = {};
            }

            const fields = details?.fieldErrors
                ? Object.values(details.fieldErrors).join('; ')
                : '';

            throw new Error(
                fields ||
                details?.message ||
                (
                    response.status === 401 ||
                    response.status === 403
                        ? 'Требуется авторизация'
                        : `Ошибка сервера: ${response.status}`
                )
            );
        }

        if (response.status === 204) {
            return null;
        }

        const raw = await response.text();

        if (!raw) {
            return null;
        }

        try {
            return JSON.parse(raw);
        } catch (_) {
            return raw;
        }
    }

    // ==============================
    // АВТОРИЗАЦИЯ
    // ==============================

    function authUI() {
        const logged = Boolean(token);

        $('authBtn').hidden = logged;
        $('registerBtn').hidden = logged;
        $('profileBtn').hidden = !logged;
        $('logoutBtn').hidden = !logged;

        $('who').textContent = logged
            ? 'Вы вошли'
            : '';
    }

    function openAuth(next = 'login') {
        mode = next;

        $('modalCover').hidden = false;
        $('authForm').reset();
        $('formError').textContent = '';

        renderModal();
    }

    function renderModal() {
        const signup = mode === 'register';

        $('modalTitle').textContent = signup
            ? 'Создать аккаунт'
            : 'Войти';

        $('modalDesc').textContent = signup
            ? 'Создай аккаунт, чтобы покупать билеты.'
            : 'Войди, чтобы купить билет на событие.';

        $('nameFields').hidden = !signup;

        $('submitBtn').textContent = signup
            ? 'Зарегистрироваться'
            : 'Войти';

        $('modalSwitch').replaceChildren(
            document.createTextNode(
                signup
                    ? 'Уже есть аккаунт? '
                    : 'Впервые здесь? '
            )
        );

        const button = document.createElement('button');

        button.type = 'button';

        button.textContent = signup
            ? 'Войти'
            : 'Зарегистрироваться';

        button.addEventListener('click', () => {
            mode = signup ? 'login' : 'register';

            $('authForm').reset();
            $('formError').textContent = '';

            renderModal();
        });

        $('modalSwitch').append(button);
    }

    // ==============================
    // КАРТОЧКИ МЕРОПРИЯТИЙ
    // ==============================

    function card(event, index) {
        const id = Number(event.id);

        const title = escapeHtml(
            text(event.title) || 'Без названия'
        );

        const venue = escapeHtml(
            text(event.venue) || 'Место уточняется'
        );

        const description = escapeHtml(
            text(event.description) ||
            'Описание появится позже.'
        );

        const startDateTime = event.startDateTime;

        const badge = startDateTime
            ? new Date(startDateTime).toLocaleDateString('ru-RU', {
                day: 'numeric',
                month: 'short'
            })
            : 'Дата уточняется';

        const cost = Number(event.price);

        const canBuy =
            Number.isFinite(id) &&
            id > 0 &&
            Number.isFinite(cost) &&
            cost > 0 &&
            Boolean(startDateTime) &&
            new Date(startDateTime) > new Date();

        const symbols = ['✳', '◈', '✦'];

        return `
            <article class="event-card">
                <div class="event-art">
                    <span class="event-date-badge">
                        ${escapeHtml(badge)}
                    </span>

                    <span class="art-symbol" aria-hidden="true">
                        ${symbols[index % symbols.length]}
                    </span>
                </div>

                <div class="event-content">
                    <span class="event-venue">
                        ${venue}
                    </span>

                    <h3>${title}</h3>

                    <p class="event-desc">
                        ${description}
                    </p>

                    <div class="event-bottom">
                        <div>
                            <span class="price-label">
                                ${escapeHtml(date(startDateTime))}
                            </span>

                            <span class="price">
                                ${escapeHtml(money(event.price))}
                            </span>
                        </div>

                        <button
                            class="btn btn-accent event-buy"
                            data-event-id="${id}"
                            ${canBuy
            ? ''
            : 'disabled title="Покупка недоступна"'}
                        >
                            Купить ↗
                        </button>
                    </div>
                </div>
            </article>
        `;
    }

    // ==============================
    // ФИЛЬТРАЦИЯ И СОРТИРОВКА
    // ==============================

    function render() {
        const query = $('searchInput')
            .value
            .trim()
            .toLowerCase();

        const sort = $('sortSelect').value;

        let filtered = events.filter(event => {
            const searchable = (
                text(event.title) + ' ' +
                text(event.venue) + ' ' +
                text(event.description)
            ).toLowerCase();

            return searchable.includes(query);
        });

        filtered.sort((a, b) => {
            if (sort === 'price') {
                return Number(a.price ?? Infinity) -
                    Number(b.price ?? Infinity);
            }

            if (sort === 'title') {
                return text(a.title).localeCompare(
                    text(b.title),
                    'ru'
                );
            }

            const firstDate = a.startDateTime
                ? new Date(a.startDateTime).getTime()
                : Infinity;

            const secondDate = b.startDateTime
                ? new Date(b.startDateTime).getTime()
                : Infinity;

            return firstDate - secondDate;
        });

        $('eventGrid').innerHTML = filtered
            .slice(0, visible)
            .map(card)
            .join('');

        $('showMoreBtn').hidden = filtered.length <= visible;

        $('status').textContent = filtered.length
            ? `Найдено событий: ${filtered.length}`
            : 'События не найдены. Попробуйте изменить запрос.';
    }

    // ==============================
    // ЗАГРУЗКА МЕРОПРИЯТИЙ
    // ==============================

    async function load() {
        $('status').textContent = 'Загружаем мероприятия…';

        try {
            const data = await api('/events');

            if (!Array.isArray(data)) {
                throw new Error(
                    'Неверный формат списка мероприятий'
                );
            }

            events = data;

            $('heroCount').textContent = data.length;

            visible = 9;

            render();
        } catch (error) {
            $('status').textContent = error.message;

            $('eventGrid').replaceChildren();

            $('showMoreBtn').hidden = true;
        }
    }

    // ==============================
    // ПОКУПКА БИЛЕТОВ
    // ==============================

    async function buy(id) {
        if (!token) {
            openAuth();

            toast(
                'Чтобы купить билет, войдите в аккаунт.'
            );

            return;
        }

        const button = document.querySelector(
            `[data-event-id="${id}"]`
        );

        if (button) {
            button.disabled = true;
            button.textContent = 'Создаём платёж…';
        }

        try {
            const result = await api('/payments/intent', {
                method: 'POST',
                body: {
                    eventId: id
                },
                auth: true
            });

            const url = result?.confirmationUrl;

            if (
                !url ||
                new URL(url).protocol !== 'https:'
            ) {
                throw new Error(
                    'Сервер не вернул корректную ссылку оплаты'
                );
            }

            window.location.assign(url);
        } catch (error) {
            toast(error.message);

            if (button) {
                button.disabled = false;
                button.textContent = 'Купить ↗';
            }
        }
    }

    // ==============================
    // ЛИЧНЫЙ КАБИНЕТ
    // ==============================

    function openProfile() {
        if (!token) {
            openAuth();
            return;
        }

        // Открываем страницу профиля напрямую,
        // без использования маршрута /me.

        window.location.assign('/me.html#profile');
    }

    // ==============================
    // ОБРАБОТЧИКИ СОБЫТИЙ
    // ==============================

    $('eventGrid').addEventListener('click', event => {
        const button = event.target.closest('[data-event-id]');

        if (button) {
            buy(Number(button.dataset.eventId));
        }
    });

    $('authBtn').addEventListener('click', () => {
        openAuth();
    });

    $('registerBtn').addEventListener('click', () => {
        openAuth('register');
    });

    $('modalClose').addEventListener('click', () => {
        $('modalCover').hidden = true;
    });

    $('modalCover').addEventListener('click', event => {
        if (event.target === $('modalCover')) {
            $('modalCover').hidden = true;
        }
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') {
            $('modalCover').hidden = true;
        }
    });

    // ==============================
    // РЕГИСТРАЦИЯ И ВХОД
    // ==============================

    $('authForm').addEventListener('submit', async event => {
        event.preventDefault();

        const form = event.currentTarget;
        const submit = $('submitBtn');
        const raw = new FormData(form);

        let body = {
            email: raw.get('email'),
            password: raw.get('password')
        };

        if (mode === 'register') {
            body = {
                ...body,
                firstName: raw.get('firstName'),
                lastName: raw.get('lastName')
            };
        }

        submit.disabled = true;
        $('formError').textContent = '';

        try {
            if (mode === 'register') {
                await api('/auth/register', {
                    method: 'POST',
                    body
                });

                toast('Аккаунт создан. Теперь войдите.');

                mode = 'login';

                renderModal();

                form.elements.password.value = '';
            } else {
                const data = await api('/auth/login', {
                    method: 'POST',
                    body
                });

                if (!data?.token) {
                    throw new Error(
                        'Не удалось получить токен авторизации'
                    );
                }

                token = data.token;

                sessionStorage.setItem(tokenKey, token);

                authUI();

                $('modalCover').hidden = true;

                toast('Вы успешно вошли!');
            }
        } catch (error) {
            $('formError').textContent = error.message;
        } finally {
            submit.disabled = false;
        }
    });

    // ==============================
    // ВЫХОД ИЗ АККАУНТА
    // ==============================

    $('logoutBtn').addEventListener('click', () => {
        token = null;

        sessionStorage.removeItem(tokenKey);

        authUI();

        toast('Вы вышли из аккаунта');
    });

    // ==============================
    // КНОПКА "МОЙ ПРОФИЛЬ"
    // ==============================

    $('profileBtn').addEventListener('click', openProfile);

    // ==============================
    // ОСТАЛЬНЫЕ КНОПКИ
    // ==============================

    $('refreshBtn').addEventListener('click', load);

    $('showMoreBtn').addEventListener('click', () => {
        visible += 9;
        render();
    });

    $('searchInput').addEventListener('input', () => {
        visible = 9;
        render();
    });

    $('sortSelect').addEventListener('change', render);

    // ==============================
    // ИНИЦИАЛИЗАЦИЯ
    // ==============================

    authUI();
    load();

})();
