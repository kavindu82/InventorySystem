// Fully updated add_sale.js with Edit and Delete functionality
let saleItemIndex = 0;

function addSaleItemRow(item = {}) {
    const tableBody = document.querySelector("#saleItemsTable tbody");
    const row = document.createElement("tr");

    row.innerHTML = `
     <tr>
    <input type="hidden" name="items[${saleItemIndex}].id" value="${item.id || ''}">
    <td>
        <select name="items[${saleItemIndex}].itemNo" class="form-select" onchange="fetchItemDetails(this)" required>
            <option value="">--Select Item--</option>
        </select>
    </td>
    <td><input type="text" name="items[${saleItemIndex}].itemName" class="form-control" readonly value="${item.itemName || ''}"></td>
    <td>
        <input type="number" name="items[${saleItemIndex}].quantity" class="form-control" required oninput="updateSaleAmount(this)" value="${item.quantity || ''}">
        <div class="text-muted small available-stock"></div>
    </td>
    <td><input type="number" name="items[${saleItemIndex}].sellingPrice" class="form-control" readonly value="${item.sellingPrice || ''}"></td>
    <td><input type="number" name="items[${saleItemIndex}].amount" class="form-control" readonly value="${item.amount || ''}"></td>
    <td><button type="button" class="btn btn-danger btn-sm" onclick="removeSaleItemRow(this)">🗑️</button></td>
</tr>
    `;
    tableBody.appendChild(row);
    saleItemIndex++;

    populateItemNoDropdown(row.querySelector("select[name*='itemNo']"), item.itemNo);
}

function populateItemNoDropdown(selectElement, selectedValue = "") {
    fetch("/sales/api/items")
        .then(response => response.json())
        .then(data => {
            data.forEach(item => {
                const option = document.createElement("option");
                option.value = item.itemNo;
                option.textContent = item.itemNo;
                selectElement.appendChild(option);
            });

            // If editing: preselect and fetch details so we get data-available + Available: X
            if (selectedValue) {
                selectElement.value = selectedValue;
                fetchItemDetails(selectElement); // <-- ensures quantity constraints & Available: X
            }
        })
        .catch(error => console.error("Error loading items:", error));
}


function fetchItemDetails(select) {
    const itemNo = select.value;
    const row = select.closest("tr");

    if (!itemNo) return;

    fetch(`/sales/items/${itemNo}`)
        .then(response => response.json())
        .then(data => {
            row.querySelector("input[name*='.itemName']").value = data.itemName;
            row.querySelector("input[name*='.sellingPrice']").value = data.sellingPrice;

            const qtyInput = row.querySelector("input[name*='.quantity']");
            qtyInput.setAttribute("max", data.quantity);
            qtyInput.setAttribute("data-available", data.quantity);
            row.querySelector(".available-stock").textContent = `Available: ${data.quantity}`;
        })
        .catch(error => console.error("Error fetching item details:", error));
}

function editSale(button) {
    const saleId = button.getAttribute('data-id');

    fetch(`/sales/api/${saleId}`)
        .then(response => response.json())
        .then(sale => {
            // Fill modal fields
            document.querySelector("#saleModal input[name='id']").value = sale.id;
            document.querySelector("#saleModal input[name='clientName']").value = sale.clientName;
            document.querySelector("#saleModal input[name='clientContact']").value = sale.clientContact;
            document.querySelector("#saleModal input[name='saleDate']").value = sale.saleDate;
            document.querySelector("#saleModal select[name='paymentStatus']").value = sale.paymentStatus;
            document.querySelector("#saleModal input[name='totalAmount']").value = sale.totalAmount;
            document.querySelector("#saleModal input[name='discountPercentage']").value = sale.discountPercentage;
            document.querySelector("#saleModal input[name='finalAmount']").value = sale.finalAmount;
            // Clear existing rows
            const tableBody = document.querySelector("#saleItemsTable tbody");
            tableBody.innerHTML = "";
            saleItemIndex = 0;

            // Add sale items to the table
            sale.items.forEach(item => addSaleItemRow(item));

            // Show modal
            new bootstrap.Modal(document.getElementById('saleModal')).show();
        })
        .catch(error => console.error("Error fetching sale details:", error));
}


document.addEventListener("input", function (event) {
    if (event.target.matches("input[name*='.quantity']")) {
        const qtyInput = event.target;
        const enteredQty = parseInt(qtyInput.value) || 0;
        const availableStock = parseInt(qtyInput.getAttribute("data-available")) || 0;

        if (enteredQty > availableStock) {
            qtyInput.value = availableStock;
            alert("⚠️ Quantity cannot exceed available stock!");
        }
        updateSaleAmount(qtyInput);
    }
});

function removeSaleItemRow(button) {
    button.closest("tr").remove();
    reindexSaleItemRows();
    calculateTotalAmount();
}

function reindexSaleItemRows() {
    const rows = document.querySelectorAll("#saleItemsTable tbody tr");
    rows.forEach((row, index) => {
        row.querySelectorAll("input, select").forEach(input => {
            const name = input.name;
            if (name.includes(".")) {
                const fieldName = name.substring(name.indexOf("."));
                input.name = `items[${index}]${fieldName}`;
            }
        });
    });
    saleItemIndex = rows.length;
}

function updateSaleAmount(input) {
    const row = input.closest("tr");
    const qty = parseFloat(row.querySelector("input[name*='.quantity']").value) || 0;
    const price = parseFloat(row.querySelector("input[name*='.sellingPrice']").value) || 0;
    row.querySelector("input[name*='.amount']").value = (qty * price).toFixed(2);
    calculateTotalAmount();
}

function calculateTotalAmount() {
    let total = 0;
    document.querySelectorAll("input[name*='.amount']").forEach(input => {
        total += parseFloat(input.value) || 0;
    });
    document.querySelector("input[name='totalAmount']").value = total.toFixed(2);
}

function openAddSaleModal() {
    document.querySelector("#saleForm").reset();
    document.querySelector("#saleItemsTable tbody").innerHTML = "";
    saleItemIndex = 0;

    document.getElementById("saveSaleBtn").textContent = "Save";
    new bootstrap.Modal(document.getElementById('saleModal')).show();
}
document.getElementById('saveSaleBtn').textContent = "Update";

const searchInput = document.getElementById("salesSearch");
searchInput.addEventListener("input", function () {
    const query = this.value.toLowerCase();
    const rows = document.querySelectorAll("#salesTableBody tr");
    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const match = Array.from(cells).some(td => td.textContent.toLowerCase().includes(query));
        row.style.display = match ? "" : "none";
    });
});


function openSalesDeleteModal(button) {
    const saleId = button.getAttribute("data-id");
    document.getElementById("confirmDeleteBtn").href = `/sales/delete/${saleId}`;
    new bootstrap.Modal(document.getElementById('deleteConfirmModal')).show();
}

function updateDiscountedTotal() {
    const totalField = document.querySelector("input[name='totalAmount']");
    const discountField = document.querySelector("input[name='discountPercentage']");
    const discountedTotalField = document.querySelector("input[name='finalAmount']");

    const total = parseFloat(totalField.value) || 0;
    const discountPercentage = parseFloat(discountField.value) || 0;

    let discountedTotal = total;
    if (discountPercentage > 0) {
        discountedTotal = total - (total * (discountPercentage / 100));
    }
    discountedTotalField.value = discountedTotal.toFixed(2);
}


function viewInvoice(button) {
    const saleId = button.getAttribute("data-id");
    fetch(`/sales/invoice/${saleId}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("invoiceContent").innerHTML = html;
            new bootstrap.Modal(document.getElementById('invoiceModal')).show();
        })
        .catch(error => console.error("Error loading invoice:", error));
}

function printInvoice() {
    const printContents = document.getElementById("invoiceContent").innerHTML;
    const printWindow = window.open("", "", "width=800,height=600");
    printWindow.document.write(`
        <html>
            <head>
                <title>Invoice</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        color: #000;
                        background: #fff;
                    }
                   
                    .items-table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .items-table th, .items-table td {
                        border: 1px solid #000;
                        padding: 8px;
                        text-align: center;
                    }
                    .totals {
                        text-align: right;
                        margin-top: 10px;
                    }
                </style>
            </head>
            <body>
                ${printContents}
            </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.print();
}

// Download PDF using the stashed id (mirrors your quotation code)
function downloadSalePDFFromModal() {
    const modal = document.getElementById("saleInvoiceModal");
    const id = modal.getAttribute("data-sale-id");
    if (id) {
        window.open(`/sales/pdf/${id}`, "_blank");
    } else {
        alert("Invoice ID not found.");
    }
}


