let quotationItemIndex = 0;

function addQuotationItemRow(item = {}) {
    const tableBody = document.querySelector("#quotationItemsTable tbody");
    const row = document.createElement("tr");

    row.innerHTML = `
        <td>
            <select name="items[${quotationItemIndex}].itemNo" class="form-select" onchange="fetchQuotationItemDetails(this)" required>
                <option value="">--Select Item--</option>
            </select>
        </td>
        <td><input type="text" name="items[${quotationItemIndex}].itemName" class="form-control" readonly value="${item.itemName || ''}"></td>
        <td>
            <input type="number" name="items[${quotationItemIndex}].quantity" class="form-control" required oninput="updateQuotationAmount(this)" value="${item.quantity || ''}">
        </td>
        <td><input type="number" name="items[${quotationItemIndex}].price" class="form-control" required value="${item.price || ''}" oninput="updateQuotationAmount(this)"></td>
        <td><input type="number" name="items[${quotationItemIndex}].amount" class="form-control" readonly value="${item.amount || ''}"></td>
        <td><button type="button" class="btn btn-danger btn-sm" onclick="removeQuotationItemRow(this)">🗑️</button></td>
    `;
    tableBody.appendChild(row);
    quotationItemIndex++;

    populateQuotationItemDropdown(row.querySelector("select[name*='itemNo']"), item.itemNo);
}

function populateQuotationItemDropdown(selectElement, selectedValue = "") {
    fetch("/sales/api/items")
        .then(response => response.json())
        .then(data => {
            data.forEach(item => {
                const option = document.createElement("option");
                option.value = item.itemNo;
                option.textContent = item.itemNo;
                if (item.itemNo === selectedValue) {
                    option.selected = true;
                }
                selectElement.appendChild(option);
            });
        })
        .catch(error => console.error("Error loading items:", error));
}

function fetchQuotationItemDetails(select) {
    const itemNo = select.value;
    const row = select.closest("tr");

    if (!itemNo) return;

    fetch(`/sales/items/${itemNo}`)
        .then(response => response.json())
        .then(data => {
            row.querySelector("input[name*='.itemName']").value = data.itemName;
            row.querySelector("input[name*='.price']").value = data.sellingPrice;
            updateQuotationAmount(row.querySelector("input[name*='.price']"));
        })
        .catch(error => console.error("Error fetching item details:", error));
}

function updateQuotationAmount(input) {
    const row = input.closest("tr");
    const qty = parseFloat(row.querySelector("input[name*='.quantity']").value) || 0;
    const price = parseFloat(row.querySelector("input[name*='.price']").value) || 0;
    row.querySelector("input[name*='.amount']").value = (qty * price).toFixed(2);
    calculateQuotationTotal();
}

function calculateQuotationTotal() {
    let total = 0;
    document.querySelectorAll("input[name*='.amount']").forEach(input => {
        total += parseFloat(input.value) || 0;
    });
    document.querySelector("input[name='totalAmount']").value = total.toFixed(2);
    updateQuotationTotal();
}

function updateQuotationTotal() {
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

function openAddQuotationModal() {
    document.querySelector("#quotationForm").reset();
    document.querySelector("#quotationItemsTable tbody").innerHTML = "";
    quotationItemIndex = 0;
    new bootstrap.Modal(document.getElementById('quotationModal')).show();
}

function removeQuotationItemRow(button) {
    button.closest("tr").remove();
    reindexQuotationItemRows();
    calculateQuotationTotal();
}

function reindexQuotationItemRows() {
    const rows = document.querySelectorAll("#quotationItemsTable tbody tr");
    rows.forEach((row, index) => {
        row.querySelectorAll("input, select").forEach(input => {
            const name = input.name;
            if (name.includes(".")) {
                const fieldName = name.substring(name.indexOf("."));
                input.name = `items[${index}]${fieldName}`;
            }
        });
    });
    quotationItemIndex = rows.length;
}

function viewQuotation(button) {
    const quotationId = button.getAttribute("data-id");
    fetch(`/quotation/view/${quotationId}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("invoiceContent").innerHTML = html;
            new bootstrap.Modal(document.getElementById('invoiceModal')).show();
        })
        .catch(error => console.error("Error loading quotation details:", error));
}

function printQuotation() {
    const printContents = document.getElementById("invoiceContent").innerHTML;
    const printWindow = window.open("", "", "width=800,height=600");
    printWindow.document.write(`
        <html>
            <head>
                <title>Quotation</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>
            <body>
                ${printContents}
            </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.print();
}

function downloadQuotationPDF(button) {
    const quotationId = button.getAttribute("data-id");
    window.open(`/quotation/pdf/${quotationId}`, "_blank");
}

