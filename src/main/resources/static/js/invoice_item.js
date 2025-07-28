// Auto-calculate amount when quantity or cost price changes
function calculateAmount() {
    const qty = parseFloat(document.getElementById("quantity").value) || 0;
    const price = parseFloat(document.getElementById("costPrice").value) || 0;
    document.getElementById("amount").value = (qty * price).toFixed(2);
}

document.getElementById("costPrice").addEventListener("input", calculateAmount);

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
    const id = btn.getAttribute("data-id");
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
    document.querySelector("#invoiceModal input[name='originalCostPrice']").value = row.dataset.originalCost || "";

    document.querySelector("#invoiceModal input[name='costPrice']").value = cells[8].textContent.trim();
    document.querySelector("#invoiceModal input[name='sellingPrice']").value = cells[9].textContent.trim();
    document.querySelector("#invoiceModal input[name='lowQuantity']").value = cells[10].textContent.trim();
    document.querySelector("#invoiceModal input[name='arrivalDate']").value = cells[11].textContent.trim();
    document.querySelector("#invoiceModal input[name='amount']").value = cells[12].textContent.trim();

    // Show modal
    new bootstrap.Modal(document.getElementById('invoiceModal')).show();
}


function openAddModal() {
    document.querySelector("#invoiceModal form").reset(); // clears all inputs
    document.querySelector("#invoiceModal input[name='no']").value = ""; // clear hidden ID
    document.getElementById("submitBtn").textContent = "Save Item"; // reset button text

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

async function fetchAndConvertCurrency() {
    const currency = document.getElementById("currency").value;
    const originalPrice = parseFloat(document.getElementById("originalCostPrice").value) || 0;

    if (currency === "LKR") {
        // If local currency, no conversion needed
        document.getElementById("costPrice").value = originalPrice.toFixed(2);
        calculateAmount();
        return;
    }

    try {
        const response = await fetch(`https://api.exchangerate.host/latest?base=${currency}&symbols=LKR`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();

        // Safely access nested properties
        const rate = data?.rates?.LKR;
        if (rate) {
            const convertedPrice = (originalPrice * rate).toFixed(2);
            document.getElementById("costPrice").value = convertedPrice;
        } else {
            console.warn("⚠️ Exchange rate for LKR not found. Using fallback rate.");
            const fallbackRate = 300; // fallback: 1 foreign = 300 LKR
            const convertedPrice = (originalPrice * fallbackRate).toFixed(2);
            document.getElementById("costPrice").value = convertedPrice;
        }

        calculateAmount();
    } catch (error) {
        console.error("Error fetching exchange rate:", error);
        alert("⚠️ Unable to fetch exchange rate. Please check your internet connection.");
    }
}



