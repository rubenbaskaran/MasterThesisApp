package rubenkarim.com.masterthesisapp.PermissionsManager;

interface PermissionListener {
    void permissionGranted(String[] permissions);
    void permissionDenied(String[] permissions);
}
