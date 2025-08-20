
function calculateAmount() {
    const qty = toNumber(document.getElementById("quantity").value);
    const price = toNumber(document.getElementById("originalCostPrice").value);
    document.getElementById("amount").value = formatPretty(qty * price, 2);
}
document.getElementById("quantity").addEventListener("input", calculateAmount);
document.getElementById("originalCostPrice").addEventListener("input", calculateAmount);

document.querySelector("#invoiceModal form").addEventListener("submit", () => {
    moneyIds.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = stripCommas(el.value);
    });
});

// ✅ Search functionality
const searchInput = document.getElementById("invoiceSearch");
searchInput.addEventListener("input", function () {
    const query = this.value.toLowerCase();
    const rows = document.querySelectorAll("#invoiceTableBody tr");
    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const match = Array.from(cells).some(td => td.textContent.toLowerCase().includes(query));
        row.style.display = match ? "" : "none";
    });
});

function openDeleteModal(btn) {
    const id = btn.closest("tr").dataset.id; // get from the row
    const deleteUrl = `/item/invoice/delete/${id}`;
    document.getElementById("confirmDeleteBtn").setAttribute("href", deleteUrl);
    new bootstrap.Modal(document.getElementById('deleteConfirmModal')).show();
}

function openEditModal(button) {
    const row = button.closest("tr");
    const cells = row.querySelectorAll("td");

    // Set hidden ID
    document.querySelector("#invoiceModal input[name='no']").value = row.dataset.id;

    // Standard fields
    document.querySelector("#invoiceModal select[name='itemNo']").value = cells[1].textContent.trim();
    document.querySelector("#invoiceModal select[name='itemName']").value = cells[2].textContent.trim();
    document.querySelector("#invoiceModal select[name='itemType']").value = cells[3].textContent.trim();
    document.querySelector("#invoiceModal select[name='supplierName']").value = cells[4].textContent.trim();
    document.querySelector("#invoiceModal input[name='quantity']").value = cells[5].textContent.trim();
    document.querySelector("#invoiceModal input[name='importSizeAndDimension']").value = cells[6].textContent.trim();

    // ✅ Currency and Original Cost from dataset
    document.querySelector("#invoiceModal select[name='currency']").value = row.dataset.currency || "LKR";
    // Prices/amount: table now shows formatted values; parse, then re-format for inputs
    // Original cost: prefer dataset (raw), fallback to table text (formatted)
    const originalRaw = row.dataset.originalCost || cells[7].textContent.replace(/[^\d.,-]/g, "");
    document.getElementById("originalCostPrice").value = formatPretty(originalRaw, 2);

    const costText    = cells[8].textContent.trim();   // e.g. "50,000.00"
    const sellingText = cells[9].textContent.trim();
    const amountText  = cells[12].textContent.trim();

    document.getElementById("costPrice").value    = formatPretty(costText, 2);
    document.getElementById("sellingPrice").value = formatPretty(sellingText, 2);
    document.getElementById("lowQuantity").value  = cells[10].textContent.trim();
    document.querySelector("#invoiceModal input[name='arrivalDate']").value = cells[11].textContent.trim();
    document.getElementById("amount").value       = formatPretty(amountText, 2);

    // Show modal first
    const modalEl = document.getElementById('invoiceModal');
    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();

    // When modal is fully shown, set Select2 fields with change events
    const handler = function () {
        // Item selects (use cell text exactly as stored)
        setSelect2Value("#invoiceModal select[name='itemNo']",   cells[1].textContent.trim());
        setSelect2Value("#invoiceModal select[name='itemName']", cells[2].textContent.trim());
        setSelect2Value("#invoiceModal select[name='itemType']", cells[3].textContent.trim());
        setSelect2Value("#invoiceModal select[name='supplierName']", cells[4].textContent.trim());

        // Update button label
        document.getElementById("submitBtn").textContent = "Update Item";

        // Remove one-time listener
        modalEl.removeEventListener('shown.bs.modal', handler);
    };
    modalEl.addEventListener('shown.bs.modal', handler);
}


function openAddModal() {
    const form = document.querySelector("#invoiceModal form");
    form.reset();
    document.querySelector("#invoiceModal input[name='no']").value = "";
    document.getElementById("submitBtn").textContent = "Save Item";

    // Clear Select2 fields visibly
    $('.searchable').val(null).trigger('change');

    new bootstrap.Modal(document.getElementById('invoiceModal')).show();
}

const invoiceModal = document.getElementById('invoiceModal');

invoiceModal.addEventListener('show.bs.modal', function () {
    const id = document.querySelector("#invoiceModal input[name='no']").value;
    const btn = document.getElementById("submitBtn");
    if (id && id.trim() !== "") {
        btn.textContent = "Update Item";
    } else {
        btn.textContent = "Save Item";
    }
});

/*async function fetchAndConvertCurrency() {
    const currency = document.getElementById("currency").value;
    const originalPrice = parseFloat(document.getElementById("originalCostPrice").value) || 0;

    if (currency === "LKR") {
        document.getElementById("costPrice").value = originalPrice.toFixed(2);
        calculateAmount();
        return;
    }

    try {
        const response = await fetch(`https://open.er-api.com/v6/latest/${currency}`);
        const data = await response.json();
        console.log("🌍 Exchange API Response:", data);

        const rate = data?.rates?.LKR;
        if (rate) {
            const converted = (originalPrice * rate).toFixed(2);
            document.getElementById("costPrice").value = converted;
        } else {
            alert("⚠️ LKR exchange rate not found.");
        }

        calculateAmount();
    } catch (error) {
        console.error("❌ Currency API error:", error);
        alert("⚠️ Failed to fetch exchange rate. Using fallback.");
        document.getElementById("costPrice").value = (originalPrice * 300).toFixed(2);
        calculateAmount();
    }
}*/

function initSelect2InModal() {
    if (!window.jQuery || !$.fn.select2) {
        console.error("Select2 not loaded");
        return;
    }

    $('.searchable').each(function () {
        // Avoid re-initializing the same element
        if ($(this).data('select2')) return;

        $(this).select2({
            placeholder: "Select an option",
            allowClear: true,
            width: '100%',
            // Keep dropdown inside the modal so it doesn't get clipped
            dropdownParent: $('#invoiceModal')
        });
    });

    // Optional: fix focus trap if needed
    if ($.fn.modal && $.fn.modal.Constructor) {
        $.fn.modal.Constructor.prototype._enforceFocus = function () {};
    }
}

// Init once on page load
$(initSelect2InModal);

// Ensure proper rendering when modal opens
$(document).on('shown.bs.modal', '#invoiceModal', initSelect2InModal);

function setSelect2Value(selector, value) {
    const $sel = $(selector);

    // Guard: empty or null
    if (value == null || value === "") {
        $sel.val(null).trigger('change');
        return;
    }

    // Make sure an option with this value exists; if not, add it
    if ($sel.find(`option[value="${CSS.escape(value)}"]`).length === 0) {
        // Append without duplicates
        $sel.append(new Option(value, value, false, false));
    }

    // Set & update visible Select2 text
    $sel.val(value).trigger('change');
}

function formatNumberInput(el) {
    // strip commas
    let value = el.value.replace(/,/g, "");

    // allow only digits and decimal
    if (isNaN(value) || value === "") {
        el.value = "";
        return;
    }

    // split integer/decimal parts
    let [intPart, decPart] = value.split(".");

    // format integer with commas
    intPart = parseInt(intPart, 10).toLocaleString("en-US");

    // rebuild
    el.value = decPart !== undefined ? `${intPart}.${decPart}` : intPart;
}

// attach formatter
["originalCostPrice", "costPrice", "sellingPrice", "amount"].forEach(id => {
    const el = document.getElementById(id);
    if (el) {
        el.addEventListener("input", () => formatNumberInput(el));
    }
});

document.querySelector("#invoiceModal form").addEventListener("submit", () => {
    ["originalCostPrice", "costPrice", "sellingPrice", "amount"].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.value = el.value.replace(/,/g, "");
        }
    });
});

// ---------- Helpers ----------
function stripCommas(v) {
    return (v || "").toString().replace(/,/g, "");
}
function toNumber(v) {
    const n = parseFloat(stripCommas(v));
    return isNaN(n) ? 0 : n;
}
function formatPretty(v, fractionDigits = 2) {
    const n = toNumber(v);
    return isFinite(n)
        ? n.toLocaleString("en-US", { minimumFractionDigits: fractionDigits, maximumFractionDigits: fractionDigits })
        : "";
}

// Attach live comma-formatting to money inputs
const moneyIds = ["originalCostPrice", "costPrice", "sellingPrice", "amount"];
function attachMoneyFormatters() {
    moneyIds.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;

        // allow only digits, dots, commas while typing
        el.addEventListener("input", () => {
            el.value = el.value.replace(/[^\d.,]/g, "");
            // live regrouping (optional but nice)
            const caret = el.selectionStart;
            const before = el.value;
            const raw = stripCommas(before);
            // split parts
            const parts = raw.split(".");
            const intPart = parts[0] ? parseInt(parts[0], 10).toLocaleString("en-US") : "0";
            el.value = parts.length > 1 ? `${intPart}.${parts[1]}` : intPart;
            // best-effort caret restore
            el.setSelectionRange(caret, caret);
        });

        // Show raw for easy editing on focus
        el.addEventListener("focus", () => {
            el.value = stripCommas(el.value);
        });

        // Pretty on blur
        el.addEventListener("blur", () => {
            if (el.hasAttribute("readonly") && !el.value) return;
            el.value = formatPretty(el.value, 2);
        });
    });
}
attachMoneyFormatters();