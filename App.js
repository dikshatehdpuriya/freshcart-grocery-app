// app.js — connected to the real Java backend (Servlets + JDBC + MySQL)
// Cart stays in localStorage (that part is fine to keep client-side).
// Products, login/signup, and orders now go through fetch() calls to Tomcat.

// CHANGE THIS if your Tomcat port or project name is different.
const API_BASE = "http://localhost:8080/FreshCartBackend";

const DELIVERY_FEE = 1.90;
const CART_KEY = "grocery_cart";
const SESSION_KEY = "grocery_current_user"; // now stores {id, name, email} as JSON

/* ============================================================
   AUTH HELPERS
   ============================================================ */

function getCurrentUser() {
  const raw = localStorage.getItem(SESSION_KEY);
  return raw ? JSON.parse(raw) : null;
}

function setCurrentUser(user) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(user));
}

function logout() {
  localStorage.removeItem(SESSION_KEY);
  window.location.href = "index.html";
}

function updateNavAuthUI() {
  const slot = document.querySelector("#nav-auth");
  if (!slot) return;

  const user = getCurrentUser();

  if (user) {
    slot.innerHTML = `
      <span class="nav-user">Hi, ${user.name.split(" ")[0]}</span>
      <a href="#" id="logout-link">Logout</a>
    `;
    document.querySelector("#logout-link").addEventListener("click", (e) => {
      e.preventDefault();
      logout();
    });
  } else {
    slot.innerHTML = `
      <a href="login.html">Login</a>
      <a href="signup.html">Sign Up</a>
    `;
  }
}

async function initSignupPage() {
  const form = document.querySelector("#signup-form");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = document.querySelector("#signup-error");
    errorEl.classList.add("hidden");

    const name = document.querySelector("#signup-name").value.trim();
    const email = document.querySelector("#signup-email").value.trim();
    const password = document.querySelector("#signup-password").value;

    try {
      const res = await fetch(`${API_BASE}/signup`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ name, email, password })
      });
      const data = await res.json();

      if (!data.success) {
        errorEl.textContent = data.message;
        errorEl.classList.remove("hidden");
        return;
      }

      setCurrentUser(data.user);
      window.location.href = "index.html";
    } catch (err) {
      errorEl.textContent = "Could not reach the server. Is Tomcat running?";
      errorEl.classList.remove("hidden");
    }
  });
}

async function initLoginPage() {
  const form = document.querySelector("#login-form");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = document.querySelector("#login-error");
    errorEl.classList.add("hidden");

    const email = document.querySelector("#login-email").value.trim();
    const password = document.querySelector("#login-password").value;

    try {
      const res = await fetch(`${API_BASE}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ email, password })
      });
      const data = await res.json();

      if (!data.success) {
        errorEl.textContent = data.message;
        errorEl.classList.remove("hidden");
        return;
      }

      setCurrentUser(data.user);
      const params = new URLSearchParams(window.location.search);
      window.location.href = params.get("redirect") || "index.html";
    } catch (err) {
      errorEl.textContent = "Could not reach the server. Is Tomcat running?";
      errorEl.classList.remove("hidden");
    }
  });
}

/* ============================================================
   CART HELPERS (still localStorage — this part is fine as-is)
   ============================================================ */

function getCart() {
  const raw = localStorage.getItem(CART_KEY);
  return raw ? JSON.parse(raw) : {};
}
function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
}
function clearCart() {
  localStorage.removeItem(CART_KEY);
}
function addToCart(id, qty = 1) {
  const cart = getCart();
  const key = String(id);
  cart[key] = (cart[key] || 0) + qty;
  saveCart(cart);
  updateCartBadge();
}
function updateQuantity(id, qty) {
  const cart = getCart();
  const key = String(id);
  if (qty <= 0) delete cart[key];
  else cart[key] = qty;
  saveCart(cart);
}
function removeFromCart(id) {
  const cart = getCart();
  delete cart[String(id)];
  saveCart(cart);
}
function getCartCount() {
  const cart = getCart();
  return Object.values(cart).reduce((sum, qty) => sum + qty, 0);
}
function updateCartBadge() {
  const badge = document.querySelector("#cart-count");
  if (badge) badge.textContent = getCartCount();
}

/* ============================================================
   PRODUCTS — now fetched from the backend
   ============================================================ */

let CURRENT_PRODUCTS = []; // cache of whatever was last fetched, used by cart math

async function fetchProducts(category, search) {
  const params = new URLSearchParams();
  if (category && category !== "All") params.set("category", category);
  if (search) params.set("search", search);

  const res = await fetch(`${API_BASE}/products?${params}`);
  return res.json();
}

function renderProducts(list) {
  const grid = document.querySelector("#product-grid");
  if (!grid) return;

  grid.innerHTML = "";

  if (list.length === 0) {
    grid.innerHTML = `<p class="empty-message">No products match your search.</p>`;
    return;
  }

  list.forEach((product) => {
    const card = document.createElement("div");
    card.className = "product-card";
    card.innerHTML = `
      <img src="${product.image}" alt="${product.name}" class="product-img" />
      <div class="product-info">
        <span class="product-category">${product.category}</span>
        <h3 class="product-name">${product.name}</h3>
        <p class="product-price">$${product.price.toFixed(2)} <span>${product.unit}</span></p>
        <button class="btn btn-primary add-to-cart-btn" data-id="${product.id}">
          Add to Cart
        </button>
      </div>
    `;
    grid.appendChild(card);
  });

  document.querySelectorAll(".add-to-cart-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      addToCart(btn.dataset.id);
      btn.textContent = "Added ✓";
      setTimeout(() => (btn.textContent = "Add to Cart"), 800);
    });
  });
}

async function applyFilters() {
  const searchInput = document.querySelector("#search-input");
  const activeCategoryBtn = document.querySelector(".category-btn.active");

  const searchTerm = searchInput ? searchInput.value.trim() : "";
  const category = activeCategoryBtn ? activeCategoryBtn.dataset.category : "All";

  const products = await fetchProducts(category, searchTerm);
  CURRENT_PRODUCTS = products;
  renderProducts(products);
}

async function initHomePage() {
  const grid = document.querySelector("#product-grid");
  if (!grid) return;

  grid.innerHTML = `<p class="empty-message">Loading products...</p>`;

  try {
    const products = await fetchProducts(null, null);
    CURRENT_PRODUCTS = products;
    renderProducts(products);
  } catch (err) {
    grid.innerHTML = `<p class="empty-message">Could not load products. Is the Java backend running on Tomcat?</p>`;
    return;
  }

  const searchInput = document.querySelector("#search-input");
  if (searchInput) {
    searchInput.addEventListener("input", applyFilters);
  }

  document.querySelectorAll(".category-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".category-btn").forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      applyFilters();
    });
  });
}

/* ============================================================
   CART PAGE — needs product details, so fetch the full catalog once
   ============================================================ */

async function getCartItems() {
  if (CURRENT_PRODUCTS.length === 0) {
    CURRENT_PRODUCTS = await fetchProducts(null, null);
  }
  const cart = getCart();
  return Object.keys(cart)
    .map((id) => {
      const product = CURRENT_PRODUCTS.find((p) => p.id === Number(id));
      const quantity = cart[id];
      if (!product) return null;
      return { product, quantity, lineTotal: +(product.price * quantity).toFixed(2) };
    })
    .filter(Boolean);
}

async function getCartTotals() {
  const items = await getCartItems();
  const subtotal = +items.reduce((sum, item) => sum + item.lineTotal, 0).toFixed(2);
  const delivery = items.length > 0 ? DELIVERY_FEE : 0;
  const total = +(subtotal + delivery).toFixed(2);
  return { subtotal, delivery, total };
}

async function renderCartPage() {
  const container = document.querySelector("#cart-items");
  if (!container) return;

  container.innerHTML = `<p class="empty-message">Loading cart...</p>`;
  const items = await getCartItems();

  if (items.length === 0) {
    container.innerHTML = `<p class="empty-message">Your cart is empty. <a href="index.html">Go shopping →</a></p>`;
  } else {
    container.innerHTML = "";
    items.forEach(({ product, quantity, lineTotal }) => {
      const row = document.createElement("div");
      row.className = "cart-row";
      row.innerHTML = `
        <img src="${product.image}" alt="${product.name}" class="cart-item-img" />
        <div class="cart-item-info">
          <h4>${product.name}</h4>
          <p class="muted">$${product.price.toFixed(2)} ${product.unit}</p>
        </div>
        <div class="qty-controls">
          <button class="qty-btn minus-btn" data-id="${product.id}">−</button>
          <span class="qty-value">${quantity}</span>
          <button class="qty-btn plus-btn" data-id="${product.id}">+</button>
        </div>
        <p class="cart-line-total">$${lineTotal.toFixed(2)}</p>
        <button class="remove-btn" data-id="${product.id}" title="Remove item">✕</button>
      `;
      container.appendChild(row);
    });
  }

  await renderCartSummary();
  wireCartRowEvents();
  updateCartBadge();
}

async function renderCartSummary() {
  const { subtotal, delivery, total } = await getCartTotals();
  const subtotalEl = document.querySelector("#summary-subtotal");
  const deliveryEl = document.querySelector("#summary-delivery");
  const totalEl = document.querySelector("#summary-total");
  const checkoutBtn = document.querySelector("#checkout-btn");

  if (subtotalEl) subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
  if (deliveryEl) deliveryEl.textContent = `$${delivery.toFixed(2)}`;
  if (totalEl) totalEl.textContent = `$${total.toFixed(2)}`;

  if (checkoutBtn) {
    const items = await getCartItems();
    checkoutBtn.classList.toggle("disabled", items.length === 0);
  }
}

function wireCartRowEvents() {
  document.querySelectorAll(".plus-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.dataset.id;
      const current = getCart()[id] || 0;
      updateQuantity(id, current + 1);
      renderCartPage();
    });
  });
  document.querySelectorAll(".minus-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.dataset.id;
      const current = getCart()[id] || 0;
      updateQuantity(id, current - 1);
      renderCartPage();
    });
  });
  document.querySelectorAll(".remove-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      removeFromCart(btn.dataset.id);
      renderCartPage();
    });
  });
}

/* ============================================================
   CHECKOUT PAGE
   ============================================================ */

async function renderCheckoutSummary() {
  const { subtotal, delivery, total } = await getCartTotals();
  const items = await getCartItems();

  const subtotalEl = document.querySelector("#co-subtotal");
  const deliveryEl = document.querySelector("#co-delivery");
  const totalEl = document.querySelector("#co-total");
  const itemCountEl = document.querySelector("#co-item-count");

  if (subtotalEl) subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
  if (deliveryEl) deliveryEl.textContent = `$${delivery.toFixed(2)}`;
  if (totalEl) totalEl.textContent = `$${total.toFixed(2)}`;
  if (itemCountEl) itemCountEl.textContent = items.reduce((s, i) => s + i.quantity, 0);
}

async function initCheckoutPage() {
  const form = document.querySelector("#checkout-form");
  if (!form) return;

  const user = getCurrentUser();
  if (!user) {
    window.location.href = "login.html?redirect=checkout.html";
    return;
  }

  const items = await getCartItems();
  if (items.length === 0) {
    const notice = document.querySelector("#checkout-empty-notice");
    if (notice) notice.classList.remove("hidden");
    form.classList.add("hidden");
    return;
  }

  await renderCheckoutSummary();

  const nameInput = document.querySelector("#full-name");
  if (nameInput) nameInput.value = user.name;

  const dateInput = document.querySelector("#delivery-date");
  if (dateInput) {
    const today = new Date().toISOString().split("T")[0];
    dateInput.min = today;
    dateInput.value = today;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.querySelector("#full-name").value.trim();
    const phone = document.querySelector("#phone").value.trim();
    const address = document.querySelector("#address").value.trim();
    const deliveryDate = document.querySelector("#delivery-date").value;
    const deliverySlot = document.querySelector("#delivery-slot").value;

    if (!name || !phone || !address || !deliveryDate || !deliverySlot) {
      alert("Please fill in every field before placing your order.");
      return;
    }

    const cartItems = await getCartItems();
    // Build "id:qty,id:qty" string the OrderServlet expects
    const itemsString = cartItems.map((i) => `${i.product.id}:${i.quantity}`).join(",");

    try {
      const res = await fetch(`${API_BASE}/orders`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          userId: user.id,
          name,
          phone,
          address,
          deliveryDate,
          deliverySlot,
          items: itemsString
        })
      });
      const data = await res.json();

      if (!data.success) {
        alert(data.message || "Something went wrong placing your order.");
        return;
      }

      clearCart();
      window.location.href = `orders.html?justPlaced=${data.order.orderId}`;
    } catch (err) {
      alert("Could not reach the server. Is Tomcat running?");
    }
  });
}

/* ============================================================
   ORDERS PAGE
   ============================================================ */

async function renderOrdersPage() {
  const container = document.querySelector("#orders-list");
  if (!container) return;

  const user = getCurrentUser();
  if (!user) {
    container.innerHTML = `<p class="empty-message">Please <a href="login.html?redirect=orders.html">log in</a> to see your order history.</p>`;
    return;
  }

  container.innerHTML = `<p class="empty-message">Loading orders...</p>`;

  const params = new URLSearchParams(window.location.search);
  const justPlaced = params.get("justPlaced");

  if (justPlaced) {
    const banner = document.querySelector("#order-success-banner");
    if (banner) {
      banner.classList.remove("hidden");
      banner.querySelector("#success-order-id").textContent = `#${justPlaced}`;
    }
  }

  let orders;
  try {
    const res = await fetch(`${API_BASE}/orders?userId=${user.id}`);
    orders = await res.json();
  } catch (err) {
    container.innerHTML = `<p class="empty-message">Could not load orders. Is the Java backend running?</p>`;
    return;
  }

  if (orders.length === 0) {
    container.innerHTML = `<p class="empty-message">No orders yet. <a href="index.html">Start shopping →</a></p>`;
    return;
  }

  container.innerHTML = "";
  orders.forEach((order) => {
    const card = document.createElement("div");
    card.className = "order-card";
    if (order.orderId === justPlaced) card.classList.add("highlight");

    const placedDate = new Date(order.placedAt).toLocaleString();

    const itemsHtml = order.items
      .map((item) => `<li>${item.name} × ${item.quantity} <span>$${item.lineTotal.toFixed(2)}</span></li>`)
      .join("");

    card.innerHTML = `
      <div class="order-header">
        <h3>Order #${order.orderId}</h3>
        <span class="order-date">${placedDate}</span>
      </div>
      <p class="order-delivery">Delivery: ${order.deliveryDate} · ${order.deliverySlot}</p>
      <p class="order-address">${order.customer.name} — ${order.customer.address} — ${order.customer.phone}</p>
      <ul class="order-items-list">${itemsHtml}</ul>
      <div class="order-totals">
        <span>Subtotal: $${order.subtotal.toFixed(2)}</span>
        <span>Delivery: $${order.delivery.toFixed(2)}</span>
        <strong>Total: $${order.total.toFixed(2)}</strong>
      </div>
    `;
    container.appendChild(card);
  });
}

/* ============================================================
   INIT
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {
  updateCartBadge();
  updateNavAuthUI();
  initHomePage();
  renderCartPage();
  initCheckoutPage();
  renderOrdersPage();
  initSignupPage();
  initLoginPage();
});