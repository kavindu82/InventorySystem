function fillEditModal(button) {
    document.getElementById('editCode').value = button.getAttribute('data-code');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editAddress').value = button.getAttribute('data-address');
}
function prepareDelete(button) {
    const code = button.getAttribute('data-code');
    document.getElementById('deleteForm').action = '/supplier/delete/' + code;
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