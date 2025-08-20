function fillEditModal(button) {
    document.getElementById('editCode').value = button.getAttribute('data-code');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editAddress').value = button.getAttribute('data-address');
}
function prepareDeleteSupplier(e, button) {
    if (e) { e.preventDefault(); e.stopPropagation(); }

    const code = button.getAttribute('data-code');

    // Pre-check via API so only one modal appears
    fetch(`/supplier/check-usage/${code}`)
        .then(res => res.json())
        .then(data => {
            if (data.inUse) {
                // Show info popup only
                document.getElementById('supplierInfoBody').textContent =
                    "❌ Cannot delete. This supplier is already used in Invoice Items.";
                new bootstrap.Modal(document.getElementById('supplierInfoModal')).show();
                return;
            }
            // Otherwise show the delete confirm modal
            document.getElementById('deleteForm').action = '/supplier/delete/' + code;
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        })
        .catch(() => {
            document.getElementById('supplierInfoBody').textContent =
                "⚠️ Could not verify usage. Please try again.";
            new bootstrap.Modal(document.getElementById('supplierInfoModal')).show();
        });
}

document.getElementById('supplierSearch').addEventListener('keyup', function () {
    let filter = this.value.toLowerCase();
    let rows = document.querySelectorAll('#supplierTable tbody tr');
    rows.forEach(row => {
        let code = row.cells[0].textContent.toLowerCase();
        let name = row.cells[1].textContent.toLowerCase();
        row.style.display = (code.includes(filter) || name.includes(filter)) ? '' : 'none';
    });
});