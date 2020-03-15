package rubenkarim.com.masterthesisapp.PermissionsManager;

public interface PermissionListener {
    void permissionGranted(String[] permissions);
    void permissionDenied(String[] permissions);
}
