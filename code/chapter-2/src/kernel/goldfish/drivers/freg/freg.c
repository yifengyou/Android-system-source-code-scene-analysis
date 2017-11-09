#include <linux/init.h>
#include <linux/module.h>
#include <linux/types.h>
#include <linux/fs.h>
#include <linux/proc_fs.h>
#include <linux/device.h>
#include <asm/uaccess.h>

#include "freg.h"

static int freg_major = 0;
static int freg_minor = 0;

static struct class* freg_class = NULL;
static struct fake_reg_dev* freg_dev = NULL;

static int freg_open(struct inode* inode, struct file* filp);
static int freg_release(struct inode* inode, struct file* filp);
static ssize_t freg_read(struct file* filp, char __user *buf, size_t count, loff_t* f_pos);
static ssize_t freg_write(struct file* filp, const char __user *buf, size_t count, loff_t* f_pos);

static struct file_operations freg_fops = {
        .owner = THIS_MODULE,
        .open = freg_open,
        .release = freg_release,
        .read = freg_read,
        .write = freg_write,
};

static ssize_t freg_val_show(struct device* dev, struct device_attribute* attr,  char* buf);
static ssize_t freg_val_store(struct device* dev, struct device_attribute* attr, const char* buf, size_t count);

static DEVICE_ATTR(val, S_IRUGO | S_IWUSR, freg_val_show, freg_val_store);

static int freg_open(struct inode* inode, struct file* filp) {
	struct fake_reg_dev* dev;
	
	dev = container_of(inode->i_cdev, struct fake_reg_dev, dev);
	filp->private_data = dev;

	return 0;
}

static int freg_release(struct inode* inode, struct file* filp) {
	return 0;
}

static ssize_t freg_read(struct file* filp, char __user *buf, size_t count, loff_t* f_pos) {
	ssize_t err = 0;
	struct fake_reg_dev* dev = filp->private_data;

	if(down_interruptible(&(dev->sem))) {	
		return -ERESTARTSYS;
	}

	if(count < sizeof(dev->val)) {
		goto out;
	}

	if(copy_to_user(buf, &(dev->val), sizeof(dev->val))) {
		err = -EFAULT;
		goto out;
	}

	err = sizeof(dev->val);

out:
	up(&(dev->sem));
	return err;
}

static ssize_t freg_write(struct file* filp, const char __user *buf, size_t count, loff_t* f_pos) {
	struct fake_reg_dev* dev = filp->private_data;
	ssize_t err = 0;

	if(down_interruptible(&(dev->sem))) {
                return -ERESTARTSYS;
        }

        if(count != sizeof(dev->val)) {
                goto out;
        }

	if(copy_from_user(&(dev->val), buf, count)) {
		err = -EFAULT;
		goto out;
	}

	err = sizeof(dev->val);

out:
	up(&(dev->sem));
	return err;
}

static ssize_t __freg_get_val(struct fake_reg_dev* dev, char* buf) {
	int val = 0;

	if(down_interruptible(&(dev->sem))) {
                return -ERESTARTSYS;
        }

        val = dev->val;
        up(&(dev->sem));

        return snprintf(buf, PAGE_SIZE, "%d\n", val);
}

static ssize_t __freg_set_val(struct fake_reg_dev* dev, const char* buf, size_t count) {
	int val = 0;

        val = simple_strtol(buf, NULL, 10);

        if(down_interruptible(&(dev->sem))) {
                return -ERESTARTSYS;
        }

        dev->val = val;
        up(&(dev->sem));

	return count;
}

static ssize_t freg_val_show(struct device* dev, struct device_attribute* attr, char* buf) {
	struct fake_reg_dev* hdev = (struct fake_reg_dev*)dev_get_drvdata(dev);
	
        return __freg_get_val(hdev, buf);
}

static ssize_t freg_val_store(struct device* dev, struct device_attribute* attr, const char* buf, size_t count) {
	 struct fake_reg_dev* hdev = (struct fake_reg_dev*)dev_get_drvdata(dev);

        return __freg_set_val(hdev, buf, count);
}


static ssize_t freg_proc_read(char* page, char** start, off_t off, int count, int* eof, void* data) {
	if(off > 0) {
		*eof = 1;
		return 0;
	}

	return __freg_get_val(freg_dev, page);	
}

static ssize_t freg_proc_write(struct file* filp, const char __user *buff, unsigned long len, void* data) {	
	int err = 0;
	char* page = NULL;

	if(len > PAGE_SIZE) {
		printk(KERN_ALERT"The buff is too large: %lu.\n", len);
		return -EFAULT;
	}

	page = (char*)__get_free_page(GFP_KERNEL);
	if(!page) {
                printk(KERN_ALERT"Failed to alloc page.\n");
		return -ENOMEM;
	}
	
	if(copy_from_user(page, buff, len)) {
		printk(KERN_ALERT"Failed to copy buff from user.\n");
                err = -EFAULT;
		goto out;
	}

	err = __freg_set_val(freg_dev, page, len);

out:
	free_page((unsigned long)page);
	return err;	
}

static void freg_create_proc(void) {
	struct proc_dir_entry* entry;
	
	entry = create_proc_entry(FREG_DEVICE_PROC_NAME, 0, NULL);
	if(entry) {
		entry->owner = THIS_MODULE;
		entry->read_proc = freg_proc_read;
		entry->write_proc = freg_proc_write;
	}
}

static void freg_remove_proc(void) {
	remove_proc_entry(FREG_DEVICE_PROC_NAME, NULL);
}


static int  __freg_setup_dev(struct fake_reg_dev* dev) {
	int err;
	dev_t devno = MKDEV(freg_major, freg_minor);

	memset(dev, 0, sizeof(struct fake_reg_dev));

	cdev_init(&(dev->dev), &freg_fops);
	dev->dev.owner = THIS_MODULE;
	dev->dev.ops = &freg_fops;

	err = cdev_add(&(dev->dev),devno, 1);
	if(err) {
		return err;
	}	

	init_MUTEX(&(dev->sem));
	dev->val = 0;

	return 0;
}

static int __init freg_init(void) { 
	int err = -1;
	dev_t dev = 0;
	struct device* temp = NULL;

	printk(KERN_ALERT"Initializing freg device.\n");

	err = alloc_chrdev_region(&dev, 0, 1, FREG_DEVICE_NODE_NAME);
	if(err < 0) {
		printk(KERN_ALERT"Failed to alloc char dev region.\n");
		goto fail;
	}

	freg_major = MAJOR(dev);
	freg_minor = MINOR(dev);

	freg_dev = kmalloc(sizeof(struct fake_reg_dev), GFP_KERNEL);
	if(!freg_dev) {
		err = -ENOMEM;
		printk(KERN_ALERT"Failed to alloc freg device.\n");
		goto unregister;
	}

	err = __freg_setup_dev(freg_dev);
	if(err) {
		printk(KERN_ALERT"Failed to setup freg device: %d.\n", err);
		goto cleanup;
	}

	freg_class = class_create(THIS_MODULE, FREG_DEVICE_CLASS_NAME);
	if(IS_ERR(freg_class)) {
		err = PTR_ERR(freg_class);
		printk(KERN_ALERT"Failed to create freg device class.\n");
		goto destroy_cdev;
	}

	temp = device_create(freg_class, NULL, dev, "%s", FREG_DEVICE_FILE_NAME);
	if(IS_ERR(temp)) {
		err = PTR_ERR(temp);
		printk(KERN_ALERT"Failed to create freg device.\n");
		goto destroy_class;
	}

	err = device_create_file(temp, &dev_attr_val);
	if(err < 0) {
		printk(KERN_ALERT"Failed to create attribute val of freg device.\n");
                goto destroy_device;
	}

	dev_set_drvdata(temp, freg_dev);

	freg_create_proc();

	printk(KERN_ALERT"Succedded to initialize freg device.\n");

	return 0;

destroy_device:
	device_destroy(freg_class, dev);
destroy_class:
	class_destroy(freg_class);
destroy_cdev:
	cdev_del(&(freg_dev->dev));	
cleanup:
	kfree(freg_dev);
unregister:
	unregister_chrdev_region(MKDEV(freg_major, freg_minor), 1);	
fail:
	return err;
}

static void __exit freg_exit(void) {
	dev_t devno = MKDEV(freg_major, freg_minor);

	printk(KERN_ALERT"Destroy freg device.\n");
	
	freg_remove_proc();

	if(freg_class) {
		device_destroy(freg_class, MKDEV(freg_major, freg_minor));
		class_destroy(freg_class);
	}

	if(freg_dev) {
		cdev_del(&(freg_dev->dev));
		kfree(freg_dev);
	}

	unregister_chrdev_region(devno, 1);
}

MODULE_LICENSE("GPL");
MODULE_DESCRIPTION("Fake Register Driver");

module_init(freg_init);
module_exit(freg_exit);

